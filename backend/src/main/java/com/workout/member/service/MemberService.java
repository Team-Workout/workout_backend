package com.workout.member.service;

import com.workout.auth.dto.SignupRequest;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.MemberErrorCode;
import com.workout.gym.domain.Gym;
import com.workout.member.domain.Member;
import com.workout.member.dto.MemberSettingsDto;
import com.workout.member.repository.MemberRepository;
import com.workout.notification.event.TokenCleanupEvent;
import com.workout.utils.service.FileService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Async
  @EventListener
  @Transactional
  public void handleTokenCleanup(TokenCleanupEvent event) {
    String invalidToken = event.invalidToken();
    log.info("비동기 리스너 수신: FCM 토큰 삭제 작업 시작. Token={}", invalidToken);

    try {
      // 해당 토큰을 가진 회원을 찾아 토큰 값만 null로 업데이트합니다.
      // (회원을 삭제하는 것이 아님에 유의)
      Member member = memberRepository.findByFcmToken(invalidToken).orElseThrow(() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));

      if (member != null) {
        member.setFcmToken(null);
        memberRepository.save(member);
        log.info("토큰 삭제 완료. MemberId={}", member.getId());
      } else {
        log.warn("삭제할 토큰을 찾았으나, DB에 해당 토큰을 가진 회원이 없습니다. Token={}", invalidToken);
      }
    } catch (Exception e) {
      log.error("FCM 토큰 비동기 삭제 중 오류 발생. Token={}", invalidToken, e);
    }
  }
}
