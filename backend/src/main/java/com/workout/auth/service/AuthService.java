package com.workout.auth.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.auth.dto.SignupRequest;
import com.workout.auth.dto.SocialSignupInfo;
import com.workout.auth.dto.SocialSignupRequest;
import com.workout.gym.domain.Gym;
import com.workout.gym.service.GymService;
import com.workout.member.domain.AccountStatus;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.service.MemberService;
import com.workout.trainer.service.TrainerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

  private final SecurityContextRepository securityContextRepository;
  private final MemberService memberService;
  private final GymService gymService;
  private final TrainerService trainerService;
  private final PasswordEncoder passwordEncoder;

  public AuthService(SecurityContextRepository securityContextRepository,
      MemberService memberService, TrainerService trainerService,
      GymService gymService, PasswordEncoder passwordEncoder) {
    this.securityContextRepository = securityContextRepository;
    this.memberService = memberService;
    this.gymService = gymService;
    this.trainerService = trainerService;
    this.passwordEncoder = passwordEncoder;
  }


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

  @Transactional
  public Long signup(SignupRequest signupRequest, Role role,HttpServletRequest request, HttpServletResponse response) {
    memberService.ensureEmailIsUnique(signupRequest.email());

    Gym gym = gymService.findById(signupRequest.gymId());

    Member newMember;
    if (role == Role.MEMBER) {
      newMember = memberService.createMember(signupRequest, gym);
    } else if (role == Role.TRAINER) {
      newMember = trainerService.createTrainer(signupRequest, gym);
    } else {
      throw new IllegalArgumentException("지원하지 않는 사용자 역할입니다.");
    }

    // 5. 수동으로 로그인 처리
    UserPrincipal userPrincipal = new UserPrincipal(newMember);
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        userPrincipal, null, userPrincipal.getAuthorities());

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    securityContextRepository.saveContext(context, request, response);
    return newMember.getId();
  }

  @Transactional
  public void completeSocialSignup(SocialSignupInfo socialSignupInfo, SocialSignupRequest requestDto,
      HttpServletRequest request, HttpServletResponse response) {
    Member newMember = memberService.createMember(socialSignupInfo, requestDto);

    // 5. 수동으로 로그인 처리
    UserPrincipal userPrincipal = new UserPrincipal(newMember);
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        userPrincipal, null, userPrincipal.getAuthorities());

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    securityContextRepository.saveContext(context, request, response);
  }
}