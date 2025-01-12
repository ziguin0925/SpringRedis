package org.example.couponcore.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public Boolean zAdd(final String key, final String value, double score) {
        // ZADD NX - sorted set에 해당 key가 없는 경우만 요청이 감.
        return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

    public Long sAdd(final String key, final String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    // set의 size
    public Long sCard(final String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // 해당 set에 존재하는지
    public Boolean sIsMember(final String key, final String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }


    public Long rPush(final String key, final String value){
        return redisTemplate.opsForList().rightPush(key, value);
    }
}
