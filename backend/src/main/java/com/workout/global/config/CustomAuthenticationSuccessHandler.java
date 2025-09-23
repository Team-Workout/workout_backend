package com.workout.global.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    // 사용자가 "ROLE_GUEST" 권한을 가지고 있는지 확인
    if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_GUEST"))) {
      // 신규 사용자면 추가 정보 입력 페이지로 리디렉션
      response.sendRedirect("/signup-social"); // 프론트엔드 URL
    } else {
      // 기존 사용자면 메인 페이지로 리디렉션
      response.sendRedirect("/"); // 프론트엔드 URL
    }
  }
}