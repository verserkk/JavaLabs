package com.example.animeservice.aspect;

import com.example.animeservice.exception.EntityNotFoundException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;



@Aspect
@Component
@Slf4j
public class LoggingAspect {


    @Around("within(com.example.animeservice.controller..*) || "
            + "within(com.example.animeservice.exceptionhandler..*)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        String packageName = joinPoint.getSignature().getDeclaringType().getPackage().getName();

        if (packageName.contains("exceptionhandler")) {
            String exceptionMessage = extractExceptionMessage(args);
            if (methodName.contains("handleEntityNotFoundException")
                    || methodName.contains("handleIllegalArgumentException")) {
                log.warn("Handling exception in method: {} with message: {}",
                        methodName, exceptionMessage);
            } else if (methodName.contains("handleGenericException")) {
                log.error("Handling exception in method: {} with message: {}",
                        methodName, exceptionMessage);
            }
        } else {
            log.info("Entering method: {} with arguments: {}", methodName, Arrays.toString(args));
        }

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            if (!packageName.contains("exceptionhandler")) {
                log.info("Exiting method: {} with result: {} (execution time: {}ms)",
                        methodName, result, executionTime);
            }

            return result;
        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    @AfterThrowing(
            pointcut = "within(com.example.animeservice.controller..*)",
            throwing = "ex"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().toShortString();
        if (ex instanceof EntityNotFoundException || ex instanceof IllegalArgumentException) {
            log.warn("Exception in method: {} with message: {}", methodName, ex.getMessage());
        } else {
            log.error("Exception in method: {} with message: {}", methodName, ex.getMessage());
        }
    }

    private String extractExceptionMessage(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Throwable) {
                return ((Throwable) arg).getMessage();
            }
        }
        return "Unknown exception message";
    }
}