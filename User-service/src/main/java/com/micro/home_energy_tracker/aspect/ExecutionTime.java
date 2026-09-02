package com.micro.home_energy_tracker.aspect;

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
    
    @Pointcut("execution(* com.micro.home_energy_tracker.service.*.*(..))")
    public void userServiceMethods() {
    }

    @Around("userServiceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long endTime = System.nanoTime();
            long executionTime = endTime - startTime;
            log.info("Execution time of {}: {} ns", joinPoint.getSignature().toShortString(), executionTime);
        }
    }
}