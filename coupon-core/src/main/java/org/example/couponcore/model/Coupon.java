package org.example.couponcore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "coupons")
public class Coupon extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CouponType couponType;



    private Integer totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minAvailableAmount;

    @Column(nullable = false)
    private LocalDateTime dateIssueStart;

    @Column(nullable = false)
    private LocalDateTime dateIssueEnd;

    // 수량 검증 : 발급해도 되면 true
    public boolean availableIssueQuantity(){

        // 발급 되어야 할 수량이 없다면.
        if(totalQuantity == null){
            return true;
        }
        // 발급 되어야 할 수량보다 발급된 수량이 많은지 검증.
        return totalQuantity > issuedQuantity;
    }

    // 기한 검증
    public boolean availableIssueDate(){
        LocalDateTime now = LocalDateTime.now();


        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    // 발급 기간이 지났는지, 발급 가능 수량이 맞는지
    public boolean isIssueComplete(){
        LocalDateTime now = LocalDateTime.now();
        return dateIssueEnd.isBefore(now) || !availableIssueQuantity();
    }



    public void issue(){

        if(!availableIssueQuantity()){
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY,
                    "발급 가능한 수량을 초과합니다. total : %s, issued : %s"
                            .formatted(totalQuantity, issuedQuantity));
        }
        if (!availableIssueDate()){
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE,
                    "발급 가능한 일자가 아닙니다. request : %s, issueStart : %s, issueEnd : %s "
                            .formatted(LocalDateTime.now(), dateIssueStart, dateIssueEnd)
            );
        }
        issuedQuantity++;
    }
}
