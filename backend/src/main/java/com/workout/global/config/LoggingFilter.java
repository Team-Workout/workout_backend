package com.workout.global.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class LoggingFilter implements Filter {

  // 로거를 추가합니다.
  private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    // HttpServletRequest/Response로 캐스팅하고, 응답을 캐싱할 수 있는 Wrapper 클래스로 감싸줍니다.
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(
        (HttpServletResponse) response);

    String traceId = UUID.randomUUID().toString().substring(0, 8);
    MDC.put("traceId", traceId);

    // MDC에 다른 정보들도 추가할 수 있습니다.
    MDC.put("requestURI", httpRequest.getRequestURI());
    MDC.put("requestMethod", httpRequest.getMethod());

    long startTime = System.currentTimeMillis();

    try {
      // 원본 response 대신 responseWrapper를 다음 필터로 전달합니다.
      chain.doFilter(request, responseWrapper);
    } finally {
      long endTime = System.currentTimeMillis();
      long duration = endTime - startTime;

      // 응답에서 상태 코드와 본문 내용을 가져옵니다.
      int status = responseWrapper.getStatus();
      String responseBody = getResponseBody(responseWrapper);

      // Access Log를 INFO 레벨로 기록합니다.
      log.info("[ACCESS LOG] method={}, uri={}, status={}, duration={}ms, responseBody={}",
          httpRequest.getMethod(),
          httpRequest.getRequestURI(),
          status,
          duration,
          responseBody);

      // ★ 매우 중요: 캐싱된 응답 내용을 실제 response에 복사하여 클라이언트에게 전달합니다.
      responseWrapper.copyBodyToResponse();

      MDC.clear();
    }
  }

  // 응답 본문을 문자열로 변환하는 헬퍼 메서드
  private String getResponseBody(ContentCachingResponseWrapper response) {
    byte[] content = response.getContentAsByteArray();
    if (content.length == 0) {
      return "";
    }
    return new String(content, StandardCharsets.UTF_8);
  }
}