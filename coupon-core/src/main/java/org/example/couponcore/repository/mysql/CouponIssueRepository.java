package org.example.couponcore.repository.mysql;

import static org.example.couponcore.model.QCouponIssue.couponIssue;

import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.couponcore.model.CouponIssue;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CouponIssueRepository {

    private final JPQLQueryFactory queryFactory;


    public CouponIssue findFirstCouponIssue(long couponId, long userId) {

        return queryFactory.selectFrom(couponIssue)
                .where(couponIssue.couponId.eq(couponId))
                .where(couponIssue.userId.eq(userId))
                .fetchFirst();

    }


}
