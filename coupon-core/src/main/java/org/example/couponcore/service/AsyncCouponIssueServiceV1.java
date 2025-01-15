package org.example.couponcore.service;

import static org.example.couponcore.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.couponcore.component.DistributeLockExecutor;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.repository.redis.RedisRepository;
import org.example.couponcore.repository.redis.dto.CouponIssueRequest;
import org.example.couponcore.repository.redis.dto.CouponRedisEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncCouponIssueServiceV1 {
    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final CouponCacheService couponCacheService;

    private final ObjectMapper objectMapper = new ObjectMapper();



    public void issue(long couponId, long userId) {

        // 쿠폰 존재 여부 확인(캐시를 통해 확인)
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);

        // 쿠폰 날짜 유효성 확인.
        coupon.checkIssuableCoupon();


        // Redis command 자체는 싱글스레드이므로 동시성 문제가 없지만 Redis 사용 과정을 분리하였기 때문에 동시성 문제가 발생할 수 있음.
        distributeLockExecutor. exceute("lock %s".formatted(couponId),3000, 3000, () ->{

            // 발급 가능한 수량과 이미 발급된 쿠폰를 검사
            couponIssueRedisService.checkCouponIssueQuantity(coupon, userId);
            // queue 에 적재
            issueRequest(couponId, userId);
        });

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
