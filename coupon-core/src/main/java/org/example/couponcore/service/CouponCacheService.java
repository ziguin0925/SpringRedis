package org.example.couponcore.service;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.model.Coupon;
import org.example.couponcore.repository.redis.dto.CouponRedisEntity;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponCacheService {
    private final CouponIssueService couponIssueService;

    /**
     * redis에 해당 쿠폰이 있으면 redis로부터 가져오고
     * redis에 해당 쿠폰이 없으면 데이터베이스로 부터 가져와서 redis에 올림.
     */
    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(Long couponId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);
        return new CouponRedisEntity(coupon);

    }

    /**
     * LocalCacheConfiguration에 있는 Caffeine 캐시를 사용.
     * LocalCache를 통해 가져옴.
     * LocalCache가 없으면 Redis로부터 가져옴
     */
    @Cacheable(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedisEntity getCouponLocalCache(long couponId) {

        // 메서드 내부에서 getCouponCache를 호출하므로 AOP를 사용하기 위해서 Proxy로 붙어야함.
        // CouponCoreConfiguration에 @EnableAspectJAutoProxy(exposeProxy = true) 설정
        // Redis로 들어가는 트래픽을 줄이기 위해 사용

        return proxy().getCouponCache(couponId);
    }


    // Redis cache를 없앤다(없어지는거로 put을 해준다.)
    @CachePut(cacheNames = "coupon")
    public CouponRedisEntity putCouponCache(long couponId) {
        return getCouponCache(couponId);
    }


    // Local cache를 없앤다(없어지는거로 put을 해준다.)
    @CachePut(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedisEntity putCouponLocalCache(long couponId) {
        return getCouponLocalCache(couponId);
    }



    // getCouponLocalCache에 프록시를 붙이기 위해서
    private CouponCacheService proxy(){
        return ((CouponCacheService) AopContext.currentProxy());
    }
}
