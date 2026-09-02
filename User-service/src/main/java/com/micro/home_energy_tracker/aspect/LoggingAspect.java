    package com.micro.home_energy_tracker.aspect;

    import org.aspectj.lang.JoinPoint;
    import org.aspectj.lang.annotation.Aspect;
    import org.aspectj.lang.annotation.Before;
    import org.aspectj.lang.annotation.Pointcut;
    import org.springframework.stereotype.Component;

    import lombok.extern.slf4j.Slf4j;


    @Aspect
    @Component
    @Slf4j  
    public class LoggingAspect {
        
        @Pointcut("execution(* com.micro.home_energy_tracker.service.*.*(..))")
        public void userServiceMethods() {
        }
        
        @Before("userServiceMethods()")
        public void logBeforeUserServiceMethods(JoinPoint joinPoint) {
                    log.info("Executing method: {} with arguments: {}", joinPoint.getSignature().toShortString(), joinPoint.getArgs());
        }
    }
