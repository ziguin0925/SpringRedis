package org.example.couponapi;

import org.example.couponcore.CouponCoreConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(CouponCoreConfiguration.class)
@SpringBootApplication
public class CouponApiApplication {

    public static void main(String[] args) {
        System.setProperty("spring.config.name", "application-core,application-coupon-api"); // yml파일 설정.
        System.setProperty("spring.profiles.active", "local"); // 코드레벨에서 profile 강제 설정.
        SpringApplication.run(CouponApiApplication.class, args);
    }

}
