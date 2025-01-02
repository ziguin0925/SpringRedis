package org.example.springredis.rest.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.springredis.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueueService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    //%s를 넣은 이유 - 필요에 따라 큐를 여러개 이용할 수 있게끔
    private final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";

    private final String USER_QUEUE_PROCEED_KEY = "users:queue:%s:proceed";

    private final String USER_QUEUE_WAIT_KEY_FOR_SCAN = "users:queue:*:wait";

    @Value("${scheduler.enabled}")
    private Boolean scheduling = false;


    /**
     * 대기열 등록
     * */
    public Mono<Long> registerWaitQueue(final String queue, final Long userId) {

        long unixTimestamp = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString(), unixTimestamp) // 저장되면 true를 반환함.(Set)
                .filter(i -> i) // true인지 false인지 구분 (이미 등록이 된 경우 false)
                .switchIfEmpty(Mono.error(ErrorCode.QUEUE_ALREADY_REGISTERED_USER2.build(queue))) // filter에서 false인 경우
                .flatMap(i -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString())) // filter의 i가 true인 경우 해당 유저의 순번을 알려줌.
                .map(i -> i >= 0 ? i + 1 : i)// 순번은 0 부터 시작되므로 +1을 해줌.
                ;
    }


    /**
     * 진입이 가능한 상태인지 조회
     * */
    public Mono<Boolean> isAllowed(final String queue, final Long userId) {
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_PROCEED_KEY.formatted(queue), userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0);
    }

    /**
     * */
    public Mono<Boolean> isAllowedByToken(final String queue, final Long userId, final String token) {

        return this.generateToken(queue, userId) // 토큰을 만들고
                .filter(gen -> gen.equalsIgnoreCase(token)) // queue이름과 userId기반으로 같은 토큰인지 확인.
                .map(i -> true)
                .defaultIfEmpty(false);
    }


    /**
     * 지정한 숫자만큼 진입을 허용
     * */
    public Mono<Long> allowUser(final String queue, final Long count){

        // wait queue에서 먼저 들어간 member 제거 후 proceed queue에 넣기.
        return reactiveRedisTemplate.opsForZSet().popMin(USER_QUEUE_WAIT_KEY.formatted(queue), count)
                .flatMap(member -> reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_PROCEED_KEY.formatted(queue),
                        member.getValue(), Instant.now().getEpochSecond()))
                .count();
    }

    public Mono<Long> getRank(final String queue, final Long userId){
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0 ? rank + 1 :rank);

    }

    @Scheduled(initialDelay = 5000 ,fixedDelay = 3000) //서버 시작후 5초 뒤에 3초 마다 해당 메서드 실행.
    public void scheduleAllowUser(){
        if(!scheduling){
            log.info("passed scheduleAllowUser...");
            return; // test코드 실행중일 때는 스케줄 동작 하지 않도록.
        }

        Long maxAllowedUserCount = 100L;

        // SCAN 0 MATCH users:queue:*:wait COUNT 100
        reactiveRedisTemplate.scan(ScanOptions.scanOptions()
                    .match(USER_QUEUE_WAIT_KEY_FOR_SCAN) // users:queue:*:wait와 일치하는 것.
                    .count(100) // users:queue:default:wait, users:queue:2:wait ... 100개 까지
                    .build())
                .map(key -> key.split(":")[2]) // default, 2 ...
                .flatMap(queue -> allowUser(queue, maxAllowedUserCount).map(allowed -> Tuples.of(queue, allowed))) // 진입 허용 - proceed queue로 이동
                .doOnNext(tuple -> log.info("Tried %d and allowed %d members of %s queue".formatted(maxAllowedUserCount, tuple.getT2(), tuple.getT1())))
                .subscribe();

    }

    public Mono<String> generateToken(final String queue, final Long userId) {
        MessageDigest digest = null;

        try {
            // sha256으로 만들기.
            digest = MessageDigest.getInstance("SHA-256");
            String input = "user-queue-%s-%d".formatted(queue, userId);

            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for(byte aByte : encodedHash) {
                // 16진수 소문자 문자열로 변환하여 추가.
                hexString.append(String.format("%02x", aByte));
            }

            return Mono.just(hexString.toString());

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }


}
