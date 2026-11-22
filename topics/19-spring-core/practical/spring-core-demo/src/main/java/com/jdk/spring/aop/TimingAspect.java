package com.jdk.spring.aop;

import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/** Cross-cutting concern applied to every @Timed method without touching that method's code. */
@Aspect
@Component
public class TimingAspect {

    private final List<String> calls = new ArrayList<>();

    @Around("@annotation(com.jdk.spring.aop.Timed)")          // pointcut: methods annotated with @Timed
    public Object time(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();                          // call the real method
        } finally {
            calls.add(joinPoint.getSignature().getName() + " took " + (System.nanoTime() - start) / 1_000 + " us");
        }
    }

    public List<String> calls() {
        return calls;
    }
}
