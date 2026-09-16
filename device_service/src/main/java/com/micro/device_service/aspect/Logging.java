package com.micro.device_service.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
@Component
public class Logging {

    @Pointcut("execution(* com.micro.device_service.service.*.*(..))")
    public void deviceServiceMethods() {
    }

    @Before("deviceServiceMethods()")
    public void logBeforeDeviceServiceMethods(JoinPoint joinPoint) {
        log.info("Executing method: {} with arguments: {}", joinPoint.getSignature().toShortString(), joinPoint.getArgs());
    }
}
