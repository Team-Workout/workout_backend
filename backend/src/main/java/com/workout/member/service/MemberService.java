package com.workout.member.service;

import com.workout.auth.dto.SignupRequest;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.MemberErrorCode;
import com.workout.gym.domain.Gym;
import com.workout.gym.service.GymService;
import com.workout.member.domain.Member;
import com.workout.member.repository.MemberRepository;
import java.util.List;
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
        .orElseThrow(() -> new RestApiException(MemberErrorCode.AUTHENTICATION_FAILED));

    /*if (!passwordEncoder.matches(password, member.getPassword())) {
      throw new RestApiException(MemberErrorCode.AUTHENTICATION_FAILED);
    }*/

    return member;
  }

  public void ensureEmailIsUnique(String email) {
    if (memberRepository.existsByEmail(email)) {
      throw new RestApiException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
    }
  }

  public Member findById(Long id) {
    return memberRepository.findById(id)
        .orElseThrow(() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));
  }

  public void allowAccessWorkoutLog(Long userId) {
    Member member = memberRepository.findById(userId)
        .orElseThrow(() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));

    member.setIsOpenWorkoutRecord(true);
    memberRepository.save(member);
  }


  public void allowAccessBodyImg(Long userId) {
    Member member = memberRepository.findById(userId)
        .orElseThrow(() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));

    member.setIsOpenBodyImg(true);
    memberRepository.save(member);
  }

  public void forbidAccessWorkoutLog(Long userId) {
    Member member = memberRepository.findById(userId)
        .orElseThrow(() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));

    member.setIsOpenWorkoutRecord(false);
    memberRepository.save(member);
  }

  public void forbidAccessBodyImg(Long userId) {
    Member member = memberRepository.findById(userId)
        .orElseThrow(() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));

    member.setIsOpenBodyImg(false);
    memberRepository.save(member);
  }

  public List<Member> findByIdIn(List<Long> ids) {
    return memberRepository.findByIdIn(ids);
  }
}
