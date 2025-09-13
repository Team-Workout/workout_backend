package com.workout.global.aop;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

  @Pointcut("within(com.workout..*Controller)")
  public void controller() {
  }

  @Around("controller()")
  public Object logRequestAndResponse(ProceedingJoinPoint pjp) throws Throwable {
    HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
        RequestContextHolder.getRequestAttributes())).getRequest();

    String controllerMethod =
        pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();
    long start = System.currentTimeMillis();

    try {
      log.info("[REQUEST] Controller Method: {}", controllerMethod);

      Object result = pjp.proceed();

      long end = System.currentTimeMillis();

      log.info("[RESPONSE] Controller Method: {}, Duration: {}ms",
          controllerMethod,
          end - start);

      return result;
    } catch (Exception e) {
      long end = System.currentTimeMillis();
      log.error("[EXCEPTION] Controller Method: {}, Duration: {}ms, Message: {}",
          controllerMethod,
          end - start,
          e.getMessage());
      throw e;
    }
  }
}