package org.example.couponcore.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponcore.model.event.CouponIssueCompleteEvent;
import org.example.couponcore.service.CouponCacheService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventListner {

    private final CouponCacheService couponCacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void issueComplete(CouponIssueCompleteEvent event){
        log.info("issue complete. cache refresh start couponId : %s".formatted(event.couponId()));
        couponCacheService.putCouponCache(event.couponId());
        // 모든 서버의 local cache가 evict되지 않는다.
        couponCacheService.putCouponLocalCache(event.couponId());
        log.info("issue complete. cache refresh end couponId : %s".formatted(event.couponId()));

    }
}
