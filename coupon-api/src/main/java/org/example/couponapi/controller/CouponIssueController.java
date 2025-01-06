package org.example.couponapi.controller;

import lombok.RequiredArgsConstructor;
import org.example.couponapi.controller.dto.CouponIssueRequestDto;
import org.example.couponapi.service.CouponIssueRequestService;
import org.example.couponapi.controller.dto.CouponIssueResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponIssueController {

    private final CouponIssueRequestService couponIssueRequestService;

    @PostMapping("/v1/issue")
    public CouponIssueResponseDto issueV1(@RequestBody CouponIssueRequestDto body){
        couponIssueRequestService.issueRequestV1(body);

        // 발급에 실패하면 exception이 반환되므로 true만 넘겨줌.
        return new CouponIssueResponseDto(true, null);
    }
}
