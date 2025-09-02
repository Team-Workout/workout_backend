// AuthService.java
package com.workout.auth.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.auth.dto.SignupRequest;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.MemberErrorCode;
import com.workout.gym.domain.Gym;
import com.workout.gym.service.GymService;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import com.workout.member.service.MemberService;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.repository.TrainerRepository;
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

  private final SecurityContextRepository securityContextRepository; // SecurityContextRepository 주입
  private final MemberService memberService;
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final GymService gymService;
  private final TrainerRepository trainerRepository;

  public AuthService(SecurityContextRepository securityContextRepository,
      MemberService memberService, MemberRepository memberRepository,
      PasswordEncoder passwordEncoder, GymService gymService,
      TrainerRepository trainerRepository) {
    this.securityContextRepository = securityContextRepository;
    this.memberService = memberService;
    this.memberRepository = memberRepository;
    this.passwordEncoder = passwordEncoder;
    this.gymService = gymService;
    this.trainerRepository = trainerRepository;
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

  public Long signup(SignupRequest signupRequest, Role role) {
    if (memberRepository.existsByEmail(signupRequest.email())) {
      throw new RestApiException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
    }

    String encodedPassword = passwordEncoder.encode(signupRequest.password());
    Gym gym = gymService.findById(signupRequest.gymId());

    if (role == Role.MEMBER) {
      Member member = signupRequest.toMemberEntity(gym, encodedPassword);
      member.setIsOpenWorkoutRecord(false);
      return memberRepository.save(member).getId();
    } else if (role == Role.TRAINER) {
      Trainer trainer = signupRequest.toTrainerEntity(gym, encodedPassword);
      return trainerRepository.save(trainer).getId();
    } else {
      throw new IllegalArgumentException("지원하지 않는 사용자 역할입니다.");
    }
  }
}