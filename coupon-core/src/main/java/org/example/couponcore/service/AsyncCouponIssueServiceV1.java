package org.example.couponcore.service;

import static org.example.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;
import static org.example.couponcore.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static org.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static org.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;
import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.couponcore.component.DistributeLockExecutor;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.model.Coupon;
import org.example.couponcore.repository.redis.RedisRepository;
import org.example.couponcore.repository.redis.dto.CouponIssueRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncCouponIssueServiceV1 {
    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;

    private final ObjectMapper objectMapper = new ObjectMapper();



    public void issue(long couponId, long userId) {

        // 쿠폰 존재 여부 확인
        Coupon coupon = couponIssueService.findCoupon(couponId);

        if(!coupon.availableIssueDate()){
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE,
                    "발급 가능한 일자가 아닙니다. request : %s, issueStart : %s, issueEnd : %s "
                            .formatted(LocalDateTime.now(), coupon.getDateIssueStart(), coupon.getDateIssueEnd())
            );
        }
        // Redis command 자체는 싱글스레드이므로 동시성 문제가 없지만 Redis 사용 과정을 분리하였기 때문에 동시성 문제가 발생할 수 있음.
        distributeLockExecutor. exceute("lock %s".formatted(couponId),3000, 3000, () ->{
            if(!couponIssueRedisService.availableTotalIssueQuantity(coupon.getTotalQuantity(), userId)){
                throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다. couponID = %s, userId = %s".formatted(couponId, userId));
            }

            if(!couponIssueRedisService.availbleUserIssueQuantity(coupon.getTotalQuantity(), userId)){
                throw new CouponIssueException(DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다. couponID = %s, userId = %s".formatted(couponId, userId));
            }
        });


        issueRequest(couponId, userId);


    }

    private void issueRequest(long couponId, long userId) {

        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);

        try{
            // String type으로 직렬화
            String value = objectMapper.writeValueAsString(issueRequest);

            // 발급 수량 제어와 요청의 유니크함을 관리 하므로 redis list의 key와 같으면 안됨.
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));

            // 쿠폰 발급 대기열에 사용.(queue)
            redisRepository.rPush(getIssueRequestQueueKey(), value);

        } catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input : %s".formatted(issueRequest));
        }


    }
}
