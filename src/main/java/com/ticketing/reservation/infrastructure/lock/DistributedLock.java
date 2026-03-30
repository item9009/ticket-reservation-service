package com.ticketing.reservation.infrastructure.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    String key();                           // SpEL 표현식 지원

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    long waitTime() default 5L;             // 락 획득 대기 시간

    long leaseTime() default 10L;           // 락 점유 최대 시간
}
