package com.micro.device_service.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
@Component
public class ExecutionTime {

    @Pointcut("execution(* com.micro.device_service.service.*.*(..))")
    public void deviceServiceMethods() {
    }

    @Around("deviceServiceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("Execution time for {}: {} ms", joinPoint.getSignature().getName(), endTime - startTime);
        }
    }
}
