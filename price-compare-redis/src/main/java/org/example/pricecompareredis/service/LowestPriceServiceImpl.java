package org.example.pricecompareredis.service;

import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Primary
@Service
@RequiredArgsConstructor
public class LowestPriceServiceImpl implements LowestPriceService{

    private final RedisTemplate myProdPriceRedis;

    @Override
    public Set getZsetValue(String key) {
        Set mySet = new HashSet();
        mySet = myProdPriceRedis.opsForZSet().rangeWithScores(key, 0, -1);
        return mySet;
    }
}
