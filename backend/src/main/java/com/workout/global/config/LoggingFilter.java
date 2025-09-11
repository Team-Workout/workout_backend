package com.workout.global.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;

    MDC.put("requestURI", httpRequest.getRequestURI());
    MDC.put("requestMethod", httpRequest.getMethod());

    log.info(">>> Request Start");

    try {
      // 다음 필터 또는 서블릿으로 요청을 전달합니다.
      chain.doFilter(request, response);
    } finally {
      log.info("<<< Request End");
      // 요청 처리가 모두 끝난 후에는 반드시 MDC의 컨텍스트 정보를 비워줍니다.
      MDC.clear();
    }
  }
}