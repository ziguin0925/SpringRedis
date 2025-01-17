package org.example.couponconsumer.component;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collection;
import org.example.couponconsumer.TestConfig;
import org.example.couponcore.repository.redis.RedisRepository;
import org.example.couponcore.service.CouponIssueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(CouponIssueListener.class)
class CouponIssueListenerTest extends TestConfig {

    @Autowired
    CouponIssueListener sut;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    RedisRepository redisRepository;

    @MockitoBean
    CouponIssueService couponIssueService;

    @BeforeEach
    void clear() {
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);
    }


    @Test
    @DisplayName("쿠폰 발급 큐에 처리 대상이 없다면 발급을 하지 않는다.")
    void issue_1() throws JsonProcessingException {
        // given
        sut.issue();

        // when & then
        verify(couponIssueService, never()).issue(anyLong(), anyLong());
    }

    @Test
    @DisplayName("쿠폰 발급 큐에 처리 대상이 있다면 발급을 한다.")
    void issue_2() throws JsonProcessingException {
        // given
        long couponId = 1L;
        long userId = 1L;

        int totalQuantity = Integer.MAX_VALUE;

        // 해당 쿠폰에 유저 대기열 등록하기.
        redisRepository.issueRequest(couponId, userId, totalQuantity);

        // when
        sut.issue();

        // then
        verify(couponIssueService, times(1)).issue(anyLong(), anyLong());
    }

    @Test
    @DisplayName("쿠폰 발급 요청 순서에 맞게 처리된다.")
    void issue_3() throws JsonProcessingException {
        // given
        long couponId = 1L;

        long userId1 = 1L;
        long userId2= 2L;
        long userId3 = 3L;

        int totalQuantity = Integer.MAX_VALUE;

        // 해당 쿠폰에 유저 대기열 등록하기.(userId : 1->3->2)
        redisRepository.issueRequest(couponId, userId1, totalQuantity);
        redisRepository.issueRequest(couponId, userId3, totalQuantity);
        redisRepository.issueRequest(couponId, userId2, totalQuantity);

        // when
        sut.issue();

        // then
        InOrder inOrder = Mockito.inOrder(couponIssueService);
        inOrder.verify(couponIssueService, times(1)).issue(couponId, userId1);
        inOrder.verify(couponIssueService, times(1)).issue(couponId, userId3);
        inOrder.verify(couponIssueService, times(1)).issue(couponId, userId2);
    }

}