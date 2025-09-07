package com.workout.member.service;

import com.workout.auth.dto.SignupRequest;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.MemberErrorCode;
import com.workout.gym.domain.Gym;
import com.workout.member.domain.Member;
import com.workout.member.dto.MemberSettingsDto;
import com.workout.member.repository.MemberRepository;
import com.workout.utils.service.FileService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final FileService fileService;

  public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
      FileService fileService) {
    this.memberRepository = memberRepository;
    this.passwordEncoder = passwordEncoder;
    this.fileService = fileService;
  }

  public Member authenticate(String email, String password) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new RestApiException(MemberErrorCode.AUTHENTICATION_FAILED));

    if (!passwordEncoder.matches(password, member.getPassword())) {
      throw new RestApiException(MemberErrorCode.AUTHENTICATION_FAILED);
    }

    return member;
  }

  public Long createMember(SignupRequest request, Gym gym) {
    String encodedPassword = passwordEncoder.encode(request.password());
    Member member = request.toMemberEntity(gym, encodedPassword);

    member.setIsOpenWorkoutRecord(false);
    member.setIsOpenBodyImg(false);
    member.setIsOpenBodyComposition(false);
    member.setProfileImageUri(fileService.getDefaultProfileImageUrl());

    return memberRepository.save(member).getId();
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

  public Member getMemberReferenceById(Long id) {
    try {
      return memberRepository.getReferenceById(id);
    } catch (EntityNotFoundException e) {
      throw new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND);
    }
  }


  public void updatePrivacySettings(Long userId, MemberSettingsDto settingsDto) {
    Member member = findById(userId);

    member.setIsOpenWorkoutRecord(settingsDto.isOpenWorkoutRecord());
    member.setIsOpenBodyImg(settingsDto.isOpenBodyImg());
    member.setIsOpenBodyComposition(settingsDto.isOpenBodyComposition());

    memberRepository.save(member);
  }

  public void updateFcmToken(Long userId, String fcmToken) {
    Member member = findById(userId);
    member.setFcmToken(fcmToken);
    memberRepository.save(member);
  }
}
