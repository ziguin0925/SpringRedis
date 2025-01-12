package org.example.couponcore.service;

import static org.example.couponcore.exception.ErrorCode.COUPON_NOT_EXIST;
import static org.example.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.model.Coupon;
import org.example.couponcore.model.CouponIssue;
import org.example.couponcore.repository.mysql.CouponIssueJpaRepository;
import org.example.couponcore.repository.mysql.CouponIssueRepository;
import org.example.couponcore.repository.mysql.CouponJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private final CouponIssueRepository couponIssueRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;
    private final CouponJpaRepository couponJpaRepository;



    /**
     * 쿠폰 발급
     *
     * @param couponId : 유저에게 주려는 쿠폰
     * @param userId : 쿠폰을 발급 받으려는 유저
     * */
    @Transactional
    public void issue(long couponId, long userId) {
        Coupon coupon = findCouponWithLock(couponId);
        coupon.issue();
        saveCouponIssue(couponId, userId);
    }



    /**
     * 쿠폰이 DB에 있는지 확인
     *
     * @param couponId : 확인하려는 쿠폰Id
     * */
    @Transactional
    public Coupon findCouponWithLock(long couponId) {
        // coupon lock 적용
        return couponJpaRepository.findCouponWithLock(couponId).orElseThrow(
                () -> new CouponIssueException(COUPON_NOT_EXIST, "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId)));
    }

    public Coupon findCoupon(long couponId) {
        // coupon lock 적용
        return couponJpaRepository.findById(couponId).orElseThrow(
                () -> new CouponIssueException(COUPON_NOT_EXIST, "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId)));
    }


    /**
     * 유저가 쿠폰을 발급 받았다는것을 저장
     * */
    @Transactional
    public CouponIssue saveCouponIssue(long couponId, long userId) {

        checkAlreadyIssued(couponId, userId);

        CouponIssue issue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();

        return couponIssueJpaRepository.save(issue);
    }

    /**
     * 해당 유저가 이미 같은 쿠폰을 발급 받았는지 검증
     * */
    private void checkAlreadyIssued(long couponId, long userId) {
        CouponIssue issue = couponIssueRepository.findFirstCouponIssue(couponId, userId);

        if (issue != null) {
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE,
                    "이미 발급된 쿠폰 입니다. user_id : %s, coupon_id : %s".formatted(userId, couponId));
        }
    }
}

