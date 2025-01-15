package org.example.couponcore.repository.redis.dto;

import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;

public enum CouponIssueRequestCode {
    SUCCESS(1),
    DUPLICATED_COUPON_ISSUE(2),
    INVALID_COUPON_ISSUE_QUANTITY(3);

    CouponIssueRequestCode(int code) {

    }

    public static CouponIssueRequestCode find(String code){
        int codeValue = Integer.parseInt(code);
        if(codeValue == 1)return SUCCESS;
        else if(codeValue == 2)return DUPLICATED_COUPON_ISSUE;
        else if(codeValue == 3)return INVALID_COUPON_ISSUE_QUANTITY;
        throw new  IllegalArgumentException("존재하지 않는 코드입니다. code = %s".formatted(codeValue));
    }

    public static void checkRequestResult(CouponIssueRequestCode code){
        if(code == INVALID_COUPON_ISSUE_QUANTITY){
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다.");
        }else if(code == DUPLICATED_COUPON_ISSUE){
            throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급 요청된 쿠폰입니다.");
        }
    }

}
