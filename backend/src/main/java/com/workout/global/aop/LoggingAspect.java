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
    // HttpServletRequest 정보는 여전히 유용하므로 유지합니다.
    HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
        RequestContextHolder.getRequestAttributes())).getRequest();

    String controllerMethod =
        pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();
    long start = System.currentTimeMillis();

    try {
      // MDC 정보 덕분에 이 로그에는 traceId, URI, Method가 자동으로 포함됩니다.
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