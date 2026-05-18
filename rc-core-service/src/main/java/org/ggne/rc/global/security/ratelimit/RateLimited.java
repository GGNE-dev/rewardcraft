package org.ggne.rc.global.security.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    String name();                                          // 버킷 식별자 (예: "mission-complete")
    long capacity();                                        // 최대 토큰 수
    long refillSeconds();                                   // N초마다 capacity만큼 충전
    RateLimitScope scope() default RateLimitScope.USER;
}
