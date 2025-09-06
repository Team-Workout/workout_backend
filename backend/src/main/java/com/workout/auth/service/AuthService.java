package com.workout.auth.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.auth.dto.SignupRequest;
import com.workout.gym.domain.Gym;
import com.workout.gym.service.GymService;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.service.MemberService;
import com.workout.trainer.service.TrainerService;
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

  private final SecurityContextRepository securityContextRepository;
  private final MemberService memberService;
  private final GymService gymService;
  private final TrainerService trainerService;


  public AuthService(SecurityContextRepository securityContextRepository,
      MemberService memberService, TrainerService trainerService, GymService gymService) {
    this.securityContextRepository = securityContextRepository;
    this.memberService = memberService;
    this.gymService = gymService;
    this.trainerService = trainerService;
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

  public Long signup(SignupRequest signupRequest, Role role) {
    memberService.ensureEmailIsUnique(signupRequest.email());

    Gym gym = gymService.findById(signupRequest.gymId());

    if (role == Role.MEMBER) {
      return memberService.createMember(signupRequest, gym);
    } else if (role == Role.TRAINER) {
      return trainerService.createTrainer(signupRequest, gym);
    } else {
      throw new IllegalArgumentException("지원하지 않는 사용자 역할입니다.");
    }
  }
}