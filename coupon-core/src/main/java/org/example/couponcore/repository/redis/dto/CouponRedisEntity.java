package org.example.couponcore.repository.redis.dto;

import static org.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static org.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.model.Coupon;
import org.example.couponcore.model.CouponType;

public record CouponRedisEntity(
        Long id,

        CouponType couponType,

        Integer totalQuantity,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssueStart,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssueEnd,

        boolean availableIssueQuantity

) {
    // Coupon 엔티티를 이용하여 생성.
    public CouponRedisEntity(Coupon coupon) {
        this(
                coupon.getId(),
                coupon.getCouponType(),
                coupon.getTotalQuantity(),
                coupon.getDateIssueStart(),
                coupon.getDateIssueEnd(),
                coupon.availableIssueQuantity()

        );
    }

    /**
     * 쿠폰 시작 날짜가 현재보다 먼저 시작되었고, 쿠폰 마감 날짜가 현재보다 나중이면 true 반환.
     */
    private boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    public void checkIssuableCoupon() {

        if (!availableIssueQuantity()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY,
                    "모든 발급 수량이 소진되었습니다. couponId : %s".formatted(id));
        }

        if (!availableIssueDate()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE,
                    "발급 가능한 일자가 아닙니다. request : %s, issueStart : %s, issueEnd : %s "
                            .formatted(LocalDateTime.now(), dateIssueStart, dateIssueEnd)
            );
        }
    }

}
