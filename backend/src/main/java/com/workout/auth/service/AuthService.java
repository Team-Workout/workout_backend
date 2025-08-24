// AuthService.java

package com.workout.auth.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

  private final SecurityContextRepository securityContextRepository; // SecurityContextRepository 주입
  private final MemberService memberService;

  public AuthService(SecurityContextRepository securityContextRepository,
      MemberService memberService) {
    this.securityContextRepository = securityContextRepository;
    this.memberService = memberService;
  }

  // 로그인
  public Member login(String email, String password, HttpServletRequest request,
      HttpServletResponse response) {
    Member member = memberService.authenticate(email, password);

    UserPrincipal userPrincipal = new UserPrincipal(member);

    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        userPrincipal, null, userPrincipal.getAuthorities());

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    securityContextRepository.saveContext(context, request, response);
    return member;
  }
}