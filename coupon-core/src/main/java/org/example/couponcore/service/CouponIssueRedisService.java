package org.example.couponcore.service;

import static org.example.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;
import static org.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;
import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.repository.redis.RedisRepository;
import org.example.couponcore.repository.redis.dto.CouponRedisEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponIssueRedisService {

    private final RedisRepository redisRepository;

    public void checkCouponIssueQuantity(CouponRedisEntity coupon, long userId){
        if(!availableTotalIssueQuantity(coupon.totalQuantity(), coupon.id())){
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다. totalQuantity = %s, userId = %s".formatted(coupon.totalQuantity(), coupon.id()));
        }

        if(!availbleUserIssueQuantity(coupon.id(), userId)){
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다. couponId = %s, userId = %s".formatted(coupon.id(), userId));
        }
    }

    public boolean availableTotalIssueQuantity(final Integer totalQuantity, final long couponId){
        if(totalQuantity == null){
            return true;
        }
        String key = getIssueRequestKey(couponId);
        // 해당 쿠폰의 totalQuantity와 redis의 적재된 쿠폰 발급 요청수 비교.
        return totalQuantity > redisRepository.sCard(key);
    }

    public boolean availbleUserIssueQuantity(long userId, long couponId) {
        String key = getIssueRequestKey(couponId);

        return !redisRepository.sIsMember(key, String.valueOf(userId));
    }
}

