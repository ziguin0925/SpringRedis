package org.example.couponcore;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching
@EnableJpaAuditing // BaseTimeEntity를 사용하기 위한 애너테이션.
@ComponentScan
@EnableAutoConfiguration
public class CouponCoreConfiguration {
}
