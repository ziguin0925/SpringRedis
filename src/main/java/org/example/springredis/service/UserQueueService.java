package org.example.springredis.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserQueueService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<Long> registerWaitQueue(final Long userId) {

        long unixTimestamp = Instant.now().getEpochSecond();
        return redisTemplate.opsForZSet().add("user-queue", userId.toString(), unixTimestamp) // 저장되면 true를 반환함.(Set)
                .filter(i -> i) // true인지 false인지 구분 : true인 경우에만
                .switchIfEmpty(Mono.error(new RuntimeException("already registered user..."))) // filter에서 false인 경우
                .flatMap(i -> redisTemplate.opsForZSet().rank("user-queue", userId.toString())) // filter의 i가 true인 경우 해당 유저의 순번을 알려줌.
                .map(i -> i >= 0 ? i + 1 : i)// 순번은 0 부터 시작되므로 +1을 해줌.
                ;
    }
}
