package org.example.couponapi.exception;

import org.example.couponapi.controller.dto.CouponIssueResponseDto;
import org.example.couponcore.exception.CouponIssueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CouponControllerAdvice {
    @ExceptionHandler(CouponIssueException.class)
    public CouponIssueResponseDto couponIssueExceptionHandler(CouponIssueException e) {
        return new CouponIssueResponseDto(false, e.getErrorCode().message);

    }
}
