package org.example.couponcore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;

import java.util.Collection;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

class CouponIssueRedisServiceTest extends CouponIssueServiceTest {
    @Autowired
    CouponIssueRedisService couponIssueRedisService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;


    @BeforeEach
    void clear(){
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);
    }

    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 수량이 존재하면 true를 반환한다.")
    void availableTotalIssueQuantity() {
        // given
        int totalIssueQuantity = 10;
        long couponId =1;
        // when
        boolean result = couponIssueRedisService.availableTotalIssueQuantity(totalIssueQuantity, couponId);
        // then
        assertThat(result).isTrue();
    }


    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 수량이 모두 소진되면 false를 반환한다.")
    void availableTotalIssueQuantity2() {
        // given
        int totalIssueQuantity = 10;
        long couponId =1;

        IntStream.range(0, totalIssueQuantity).forEach(userId -> {
            redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(userId));
        });
        // when
        boolean result = couponIssueRedisService.availableTotalIssueQuantity(totalIssueQuantity, couponId);
        // then
        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("쿠폰 중복 검증 - 발급된 내역에 유저가 존재하지 않으면 true를 반환한다.")
    void availbleUserIssueQuantity1() {
        // given
        long couponId =1;
        long userId =1;

        // when
        boolean result = couponIssueRedisService.availbleUserIssueQuantity(userId, couponId);
        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("쿠폰 중복 검증 - 발급된 내역에 유저가 존재하면 false를 반환한다.")
    void availbleUserIssueQuantity2() {
        // given
        long couponId =1;
        long userId =1;

        redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(userId));
        // when
        boolean result = couponIssueRedisService.availbleUserIssueQuantity(userId, couponId);
        // then
        assertThat(result).isFalse();
    }

}