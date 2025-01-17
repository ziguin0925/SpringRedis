package org.example.couponcore.service;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.repository.redis.RedisRepository;
import org.example.couponcore.repository.redis.dto.CouponRedisEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncCouponIssueServiceV2 {
    private final RedisRepository redisRepository;
    private final CouponCacheService couponCacheService;


    /**
     * 요청이 오면 Redis에만 저장함.
     * Redis Sets, Lists(대기열 queue)에 저장됨.
     * 실제 대기열 queue에 저장된 유저에대해 쿠폰을 발급해주는 것은 scheduler를 통해 실행됨.
     * */
    public void issue(long couponId, long userId) {

        // 쿠폰 존재 여부 확인(캐시를 통해 확인)
        CouponRedisEntity coupon = couponCacheService.getCouponLocalCache(couponId);

        // 쿠폰 날짜 유효성 확인.
        coupon.checkIssuableCoupon();
        issueRequest(couponId, userId, coupon.totalQuantity());

        /*
         * lock에서 동작되는 기능 분석
         * 1. totalQuantity > redisRepository.sCard(key); // 쿠폰 발급 수량 검증
         * 2. redisRepository.sIsMember(key, String.valueOf(userId)); // 중복 발급 요청 제어
         * 3. redisRepository.sAdd // 쿠폰 발급 요청 저장(발급 수량 제어)
         * 4. redisRepository.rPush // 쿠폰 발급 큐에 적재
         * ->1번부터 4번까지의 과정을 한번에 묶어서 redis의 단일 커맨드로 처리 lock을 걸지 않고 한번에 처리.
         * -> redis의 script 사용.(Redis EVAL 명령어)
         * -> script에 담긴 명령어들은 하나의 원자성을 띄고 있으므로 실행이 되는 중간에 다른 커맨드가 실행 되지 않음(싱글 스레드)
         * */


    }

    private void issueRequest(long couponId, long userId, Integer totalIssueQuantity) {
        if(totalIssueQuantity == null){
            redisRepository.issueRequest(couponId,userId,Integer.MAX_VALUE);
        }

        redisRepository.issueRequest(couponId, userId, totalIssueQuantity);
    }
}
