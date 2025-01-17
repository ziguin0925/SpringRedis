package org.example.couponcore.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CouponTest {


    // file->Setting -> Editor -> LiveTemplates에서 특정코드 자동 완성 가능.
    @Test
    @DisplayName("발급 수량이 소진되었다면 false를 반환한다")
    void availableIssueQuantity_1() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .build();

        // when

        boolean result = coupon.availableIssueQuantity();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("발급 수량이 남아있다면 true를 반환한다")
    void availableIssueQuantity_2() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .build();

        // when

        boolean result = coupon.availableIssueQuantity();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("발급 수량이 정해지지 않았다면 true를 반환한다")
    void availableIssueQuantity_3() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(null)
                .issuedQuantity(99)
                .build();

        // when

        boolean result = coupon.availableIssueQuantity();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("발급 기간이 시작되지 않았다면 false를 반환한다")
    void availableIssueDate_1() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when

        boolean result = coupon.availableIssueDate();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("발급 기간에 해당되면 true를 반환한다")
    void availableIssueDate_2() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when

        boolean result = coupon.availableIssueDate();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("발급 기간이 종료되면 false를 반환한다")
    void availableIssueDate_3() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();

        // when

        boolean result = coupon.availableIssueDate();

        // then
        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("발급 기간과 발급 수량이 유효하다면 발급에 성공한다.")
    void availableIssue_1() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();

        // when

        coupon.issue();

        // then
        assertThat(coupon.getIssuedQuantity()).isEqualTo(100);
    }


    @Test
    @DisplayName("발급 수량을 초과하면 예외를 반환한다.")
    void availableIssue_2() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();

        // when, then
        assertThatThrownBy(coupon::issue)
                .isInstanceOf(CouponIssueException.class)
                .satisfies(exception -> {
                    CouponIssueException ex = (CouponIssueException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
                });
    }

    @Test
    @DisplayName("발급기간이 아니면 예외를 반환한다.")
    void availableIssue_3() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();

        // when, then
        assertThatThrownBy(coupon::issue)
                .isInstanceOf(CouponIssueException.class)
                .satisfies(exception -> {
                    CouponIssueException ex = (CouponIssueException) exception;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_COUPON_ISSUE_DATE);
                });
    }


    @Test
    @DisplayName("발급 기간이 종료되면 true를 반환한다.")
    void isIssuedComplete_1() {
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();

        // when
        boolean result = coupon.isIssueComplete();

        // then
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("잔여 발급 가능 수량이 없다면 true를 반환한다.")
    void isIssuedComplete_2() {
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();

        // when
        boolean result = coupon.isIssueComplete();

        // then
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("발급 기한과 수량이 유효하면 false를 반환한다.")
    void isIssuedComplete_3() {
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();

        // when
        boolean result = coupon.isIssueComplete();

        // then
        Assertions.assertFalse(result);
    }


}