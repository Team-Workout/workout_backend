package com.workout.pt.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import com.workout.pt.domain.contract.PTApplication;
import com.workout.pt.domain.contract.PTApplicationStatus;
import com.workout.pt.domain.contract.PTOffering;
import com.workout.pt.dto.request.PtApplicationRequest;
import com.workout.pt.dto.response.PendingApplicationResponse;
import com.workout.pt.repository.PTApplicationRepository;
import com.workout.pt.repository.PTOfferingRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

@Service
public class PTApplicationService {

  private final PTApplicationRepository ptApplicationRepository;
  private final PTContractService ptContractService;
  private final MemberRepository memberRepository;
  private final PTOfferingRepository ptOfferingRepository;

  public PTApplicationService(PTApplicationRepository ptApplicationRepository,
      MemberRepository memberRepository, PTContractService ptContractService,
      PTOfferingRepository ptOfferingRepository) {
    this.ptApplicationRepository = ptApplicationRepository;
    this.memberRepository = memberRepository;
    this.ptContractService = ptContractService;
    this.ptOfferingRepository = ptOfferingRepository;
  }

  public void createApplication(PtApplicationRequest ptApplicationRequest,
      UserPrincipal userPrincipal) {
    Member member = findMemberById(userPrincipal.getUserId());

    PTOffering ptOffering = findOfferingById(ptApplicationRequest.offeringId());

    PTApplication ptApplication = PTApplication.builder()
        .status(PTApplicationStatus.PENDING)
        .offering(ptOffering)
        .member(member).build();
    ptApplicationRepository.save(ptApplication);
  }

  public PendingApplicationResponse findPendingApplicationsForUser(UserPrincipal userPrincipal) {
    Member user = memberRepository.findById(userPrincipal.getUserId())
        .orElseThrow(
            () -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userPrincipal.getUserId()));

    List<PTApplication> pendingApplications;

    if (user.getRole() == Role.TRAINER) {
      pendingApplications = ptApplicationRepository
          .findPendingApplicationsByTrainerId(user.getId(), PTApplicationStatus.PENDING);
    } else if (user.getRole() == Role.MEMBER) {
      pendingApplications = ptApplicationRepository
          .findByMemberIdAndStatus(user.getId(), PTApplicationStatus.PENDING);
    } else {
      pendingApplications = Collections.emptyList();
    }

    return PendingApplicationResponse.from(pendingApplications);
  }

  public void acceptApplication(Long applicationId, UserPrincipal userPrincipal) {
    Member trainer = findMemberById(userPrincipal.getUserId());
    PTApplication ptApplication = findApplicationById(applicationId);

    verifyTrainerAuthorization(trainer, ptApplication);

    if (ptApplication.getStatus() != PTApplicationStatus.PENDING) {
      //throw new IllegalStateException("이미 처리되었거나 대기 중이 아닌 신청은 수락할 수 없습니다.");
      ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "이미 처리되었거나 대기 중이 아닌 신청은 수락할 수 없습니다.");
      throw new ErrorResponseException(HttpStatus.BAD_REQUEST, pd, null);
    }

    ptApplication.setStatus(PTApplicationStatus.APPROVED);

    ptApplicationRepository.save(ptApplication);

    ptContractService.createContractFromApplication(ptApplication);
  }

  public void rejectApplication(Long applicationId, UserPrincipal userPrincipal) {
    Member trainer = findMemberById(userPrincipal.getUserId());
    PTApplication ptApplication = findApplicationById(applicationId);

    verifyTrainerAuthorization(trainer, ptApplication);

    if (ptApplication.getStatus() != PTApplicationStatus.PENDING) {
      //throw new IllegalStateException("이미 처리되었거나 대기 중이 아닌 신청은 수락할 수 없습니다.");
      ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "이미 처리되었거나 대기 중이 아닌 신청은 수락할 수 없습니다.");
      throw new ErrorResponseException(HttpStatus.BAD_REQUEST, pd, null);
    }

    ptApplication.setStatus(PTApplicationStatus.REJECTED);

    ptApplicationRepository.save(ptApplication);
  }

  public void cancelApplication(Long applicationId, UserPrincipal userPrincipal) {
    Member member = findMemberById(userPrincipal.getUserId());
    PTApplication ptApplication = findApplicationById(applicationId);

    if (!ptApplication.getMember().getId().equals(member.getId())) {
      throw new AccessDeniedException("자신의 PT 신청만 취소할 수 있습니다.");
    }

    if (ptApplication.getStatus() != PTApplicationStatus.PENDING) {
      //throw new IllegalStateException("이미 처리되었거나 대기 중이 아닌 신청은 수락할 수 없습니다.");
      ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "이미 처리되었거나 대기 중이 아닌 신청은 수락할 수 없습니다.");
      throw new ErrorResponseException(HttpStatus.CONFLICT, pd, null);
    }

    ptApplication.setStatus(PTApplicationStatus.CANCELLED);

    ptApplicationRepository.save(ptApplication);
  }

  private Member findMemberById(Long userId) {
    return memberRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
  }

  private PTOffering findOfferingById(Long offeringId) {
    return ptOfferingRepository.findById(offeringId)
        .orElseThrow(() -> new EntityNotFoundException("pt정보를 찾을 수 없습니다. ID: " + offeringId));
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
    if (!application.getOffering().getTrainer().getId().equals(trainer.getId())) {
      throw new AccessDeniedException("자신에게 온 PT 신청이 아닙니다.");
    }
  }
}
