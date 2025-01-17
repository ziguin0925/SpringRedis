package org.example.couponcore.util;


public class CouponRedisUtils {

    public static String getIssueRequestKey(long couponId) {
        return "issue:request:couponId=%s".formatted(couponId);

    }

    /**
     * 대기열 Queue의 Key
     * */
    public static String getIssueRequestQueueKey() {
        return "issue:request";

    }
}