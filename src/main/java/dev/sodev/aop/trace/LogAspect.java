package dev.sodev.aop.trace;

import dev.sodev.aop.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final LogTrace logTrace;

    @Pointcut("execution(* dev.sodev.controller..*Controller*.*(..))")
    public void allService(){};

    @Pointcut("execution(* dev.sodev.service..*Service*.*(..))")
    public void allRepository(){};

    @Pointcut("execution(* dev.sodev.repository..*Repository*.*(..))")
    public void allController(){};


    @Around("allService() || allController() || allRepository()")
    public Object logTrace(ProceedingJoinPoint joinPoint) throws Throwable {

        TraceStatus status = null;

        try{

            status = logTrace.begin(joinPoint.getSignature().toShortString());
            Object result = joinPoint.proceed();

            logTrace.end(status);

            return result;
        }catch (Throwable e){
            e.printStackTrace();
            logTrace.exception(status, e);
            throw e;
        }
    }
}
