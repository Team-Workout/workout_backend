package com.workout.global.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class LoggingFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    // 고유한 요청 ID 생성 (Trace ID)
    String traceId = UUID.randomUUID().toString().substring(0, 8);
    MDC.put("traceId", traceId);

    // 요청 정보 MDC에 추가
    if (request instanceof HttpServletRequest httpRequest) {
      MDC.put("requestURI", httpRequest.getRequestURI());
      MDC.put("requestMethod", httpRequest.getMethod());
      MDC.put("clientIp", httpRequest.getRemoteAddr());
    }

    try {
      // 다음 필터 또는 서블릿으로 요청 전달
      chain.doFilter(request, response);
    } finally {
      // 요청 처리가 끝나면 반드시 MDC에서 데이터 제거
      MDC.clear();
    }
  }
}