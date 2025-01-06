package org.example.couponapi.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

// 값이 null인 경우 해당 데이터는 보내지 않음
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record CouponIssueResponseDto(boolean isSuccess, String comment) {
}
