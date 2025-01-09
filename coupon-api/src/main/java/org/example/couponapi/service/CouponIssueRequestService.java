package org.example.couponapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponapi.controller.dto.CouponIssueRequestDto;
import org.example.couponcore.component.DistributeLockExecutor;
import org.example.couponcore.service.CouponIssueService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CouponIssueRequestService {
    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;

     /*
    * lock획득
    *
    * 트랜잭션 시작
    *
    * coupon.issue()
    * saveCouponIssue()
    *
    * 트랜잭션 커밋
    *
    * lock 반납
    *
    * 1번 요청
    *
    * */
    public void issueRequestV1(CouponIssueRequestDto requestDto){
//        // synchronized - 자바에 종속, 여러 서버로 확장이 되는 순간 lock이 제대로 동작되지 않음.
//        synchronized (this){
//            couponIssueService.issue(requestDto.couponId(), requestDto.userId());
//        }

        // Redis Lock
//        distributeLockExecutor.exceute("lock_"+requestDto.couponId(),10000,10000,()->{
            couponIssueService.issue(requestDto.couponId(), requestDto.userId());
//        });

        log.info("쿠폰 발급 완료. couponId: {%s}, userId: {%s}".formatted(requestDto.couponId(), requestDto.userId()));
    }

}
