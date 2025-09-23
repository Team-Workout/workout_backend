// CustomAuthenticationFailureHandler.java (새 파일)
package com.workout.global.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

  private final CustomAuthenticationSuccessHandler successHandler;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {

    log.error("OAuth2 인증 실패. 원인: {}", exception.getMessage(), exception);

    String targetUrl = successHandler.determineTargetUrl(request);

    targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
        .queryParam("error", "true")
        .queryParam("message", exception.getLocalizedMessage())
        .build().toUriString();

    response.sendRedirect(targetUrl);
  }
}