package org.example.springredis.rest.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.example.springredis.exception.ErrorCode;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserQueueService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    //%s를 넣은 이유 - 필요에 따라 큐를 여러개 이용할 수 있게끔
    private final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";

    private final String USER_QUEUE_PROCEED_KEY = "users:queue:%s:proceed";
    private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;


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


}
