package com.ticketing.reservation.infrastructure.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private static final String LOCK_PREFIX = "LOCK:SEAT:";

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(com.ticketing.reservation.infrastructure.lock.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        DistributedLock annotation = signature.getMethod()
                .getAnnotation(DistributedLock.class);

        String lockKey = LOCK_PREFIX + parseKey(
                signature.getParameterNames(),
                joinPoint.getArgs(),
                annotation.key()
        );

        RLock lock = redissonClient.getLock(lockKey);
        log.debug("분산 락 획득 시도: {}", lockKey);

        try {
            boolean available = lock.tryLock(
                    annotation.waitTime(),
                    annotation.leaseTime(),
                    annotation.timeUnit()
            );
            if (!available) {
                throw new LockAcquisitionException("좌석이 이미 선점 중입니다: " + lockKey);
            }
            log.debug("분산 락 획득 성공: {}", lockKey);
            return aopForTransaction.proceed(joinPoint);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException("락 획득 중 인터럽트 발생", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("분산 락 해제: {}", lockKey);
            }
        }
    }

    private String parseKey(String[] paramNames, Object[] args, String key) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        return Objects.requireNonNull(
                parser.parseExpression(key).getValue(context, String.class)
        );
    }
}
