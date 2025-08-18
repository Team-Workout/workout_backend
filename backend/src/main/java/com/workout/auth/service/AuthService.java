// AuthService.java

package com.workout.auth.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final SecurityContextRepository securityContextRepository; // SecurityContextRepository 주입


  public AuthService(MemberRepository memberRepository,
      SecurityContextRepository securityContextRepository, PasswordEncoder passwordEncoder) {
    this.memberRepository = memberRepository;
    this.securityContextRepository = securityContextRepository;
    this.passwordEncoder = passwordEncoder;
  }

  // 로그인
  public Member login(String email, String password, HttpServletRequest request,
      HttpServletResponse response) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

    if (!passwordEncoder.matches(password, member.getPassword())) {
      throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    log.info("로그인 성공: {}", member.getEmail());

    UserPrincipal userPrincipal = new UserPrincipal(member);
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        userPrincipal, null, userPrincipal.getAuthorities());

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    securityContextRepository.saveContext(context, request, response);

    return member;
  }
}