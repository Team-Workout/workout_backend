package com.workout.member.service;

import com.workout.auth.dto.SignupRequest;
import com.workout.gym.domain.Gym;
import com.workout.gym.service.GymService;
import com.workout.member.domain.Member;
import com.workout.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MemberService {

  private final MemberRepository memberRepository;
  private final GymService gymService;
  private final PasswordEncoder passwordEncoder;

  public MemberService(MemberRepository memberRepository, GymService gymService,
      PasswordEncoder passwordEncoder) {
    this.memberRepository = memberRepository;
    this.gymService = gymService;
    this.passwordEncoder = passwordEncoder;
  }

  public Member authenticate(String email, String password) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

    if (!passwordEncoder.matches(password, member.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    return member;
  }

  public void ensureEmailIsUnique(String email) {
    if (memberRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("존재하는 이메일입니다");
    }
  }

  public Member registerUser(SignupRequest signupRequest) {
    Gym gym = gymService.findById(signupRequest.gymId());

    ensureEmailIsUnique(signupRequest.email());

    String encodedPassword = passwordEncoder.encode(signupRequest.password());

    Member member = signupRequest.toMemberEntity(gym, encodedPassword);

    return memberRepository.save(member);
  }
}
