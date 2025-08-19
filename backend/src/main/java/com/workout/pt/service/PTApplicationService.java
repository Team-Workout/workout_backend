package com.workout.pt.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import com.workout.pt.domain.PTApplication;
import com.workout.pt.domain.PTApplicationStatus;
import com.workout.pt.dto.PendingApplicationResponse;
import com.workout.pt.repository.PTApplicationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PTApplicationService {

  private final PTApplicationRepository ptApplicationRepository;
  private final PTContractService ptContractService;
  private final MemberRepository memberRepository;

  public PTApplicationService(PTApplicationRepository ptApplicationRepository,
      MemberRepository memberRepository, PTContractService ptContractService) {
    this.ptApplicationRepository = ptApplicationRepository;
    this.memberRepository = memberRepository;
    this.ptContractService = ptContractService;
  }

  public PendingApplicationResponse findPendingApplicationsForUser(UserPrincipal userPrincipal) {
    Member user = memberRepository.findById(userPrincipal.getUserId())
        .orElseThrow(
            () -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userPrincipal.getUserId()));

    List<PTApplication> pendingApplications;

    if (user.getRole() == Role.TRAINER) {
      pendingApplications = ptApplicationRepository
          .findByTrainerIdAndStatus(user.getId(), PTApplicationStatus.PENDING);
    } else if (user.getRole() == Role.MEMBER) {
      pendingApplications = ptApplicationRepository
          .findByMemberIdAndStatus(user.getId(), PTApplicationStatus.PENDING);
    } else {
      pendingApplications = Collections.emptyList();
    }

    return PendingApplicationResponse.from(pendingApplications);
  }

  @Transactional
  public void acceptApplication(Long applicationId, UserPrincipal userPrincipal) {
    Member trainer = findMemberById(userPrincipal.getUserId());
    PTApplication ptApplication = findApplicationById(applicationId);

    verifyTrainerAuthorization(trainer, ptApplication);

    ptApplication.setStatus(PTApplicationStatus.APPROVED);

    ptApplicationRepository.save(ptApplication);

    ptContractService.createContractFromApplication(ptApplication);
  }

  public void rejectApplication(Long applicationId, UserPrincipal userPrincipal) {
    Member trainer = findMemberById(userPrincipal.getUserId());
    PTApplication ptApplication = findApplicationById(applicationId);

    verifyTrainerAuthorization(trainer, ptApplication);

    ptApplication.setStatus(PTApplicationStatus.REJECTED);

    ptApplicationRepository.save(ptApplication);
  }


  public void cancelApplication(Long applicationId, UserPrincipal userPrincipal) {
    Member member = findMemberById(userPrincipal.getUserId());
    PTApplication ptApplication = findApplicationById(applicationId);

    if (!ptApplication.getMember().getId().equals(member.getId())) {
      throw new AccessDeniedException("자신의 PT 신청만 취소할 수 있습니다.");
    }

    ptApplication.setStatus(PTApplicationStatus.CANCELLED);
  }

  private Member findMemberById(Long userId) {
    return memberRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
  }

  private PTApplication findApplicationById(Long applicationId) {
    return ptApplicationRepository.findById(applicationId)
        .orElseThrow(
            () -> new EntityNotFoundException("PT 신청 정보를 찾을 수 없습니다. ID: " + applicationId));
  }

  private void verifyTrainerAuthorization(Member trainer, PTApplication application) {
    if (trainer.getRole() != Role.TRAINER) {
      throw new AccessDeniedException("트레이너만 이용할 수 있는 서비스입니다.");
    }
    if (!application.getTrainer().getId().equals(trainer.getId())) {
      throw new AccessDeniedException("자신에게 온 PT 신청이 아닙니다.");
    }
  }
}
