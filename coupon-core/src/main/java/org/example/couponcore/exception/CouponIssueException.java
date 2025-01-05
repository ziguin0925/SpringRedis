package org.example.couponcore.exception;


import lombok.Getter;

public class CouponIssueException extends RuntimeException{

    @Getter
    private final ErrorCode errorCode;
    private final String message;


    public CouponIssueException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "[%s] %s".formatted(errorCode, message);
    }
}
