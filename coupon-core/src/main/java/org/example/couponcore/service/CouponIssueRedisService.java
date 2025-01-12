package org.example.couponcore.service;

import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.repository.redis.RedisRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponIssueRedisService {

    private final RedisRepository redisRepository;

    public boolean availableTotalIssueQuantity(final Integer totalQuantity, final long couponId){
        if(totalQuantity == null){
            return true;
        }
        String key = getIssueRequestKey(couponId);
        // 요청의 수와 totalQuantity의 수를 비교
        return totalQuantity > redisRepository.sCard(key);
    }

    public boolean availbleUserIssueQuantity(long userId, long couponId) {
        String key = getIssueRequestKey(couponId);

        return !redisRepository.sIsMember(key,String.valueOf(userId));
    }
}

