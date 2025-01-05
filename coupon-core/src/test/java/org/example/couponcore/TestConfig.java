package org.example.couponcore;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test-coupon-core")
@TestPropertySource(properties = "spring.config.name=application-core")
@SpringBootTest(classes = CouponCoreConfiguration.class)
public class TestConfig {

}
