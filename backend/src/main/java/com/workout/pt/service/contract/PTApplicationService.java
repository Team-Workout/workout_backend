package com.workout.pt.service.contract;

import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.PTErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.service.MemberService;
import com.workout.pt.domain.contract.PTApplication;
import com.workout.pt.domain.contract.PTApplicationStatus;
import com.workout.pt.domain.contract.PTOffering;
import com.workout.pt.dto.request.PtApplicationRequest;
import com.workout.pt.dto.response.PendingApplicationResponse;
import com.workout.pt.repository.PTApplicationRepository;
import com.workout.pt.repository.PTOfferingRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PTApplicationService {

  private final PTApplicationRepository ptApplicationRepository;
  private final PTContractService ptContractService;
  private final PTOfferingRepository ptOfferingRepository;
  private final MemberService memberService;

  public PTApplicationService(PTApplicationRepository ptApplicationRepository,
      PTContractService ptContractService, PTOfferingRepository ptOfferingRepository,
      MemberService memberService) {
    this.ptApplicationRepository = ptApplicationRepository;
    this.ptContractService = ptContractService;
    this.ptOfferingRepository = ptOfferingRepository;
    this.memberService = memberService;
  }

  @Transactional
  public void createApplication(PtApplicationRequest ptApplicationRequest,
      Long userId) {
    Member member = memberService.findById(userId);

    PTOffering ptOffering = findOfferingById(ptApplicationRequest.offeringId());

    PTApplication ptApplication = PTApplication.builder()
        .status(PTApplicationStatus.PENDING)
        .offering(ptOffering)
        .member(member).build();
    ptApplicationRepository.save(ptApplication);
  }

  public PendingApplicationResponse findPendingApplicationsForUser(Long userId) {
    Member user = memberService.findById(userId);

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

  @Transactional
  public void acceptApplication(Long applicationId, Long userId) {
    Member trainer = memberService.findById(userId);
    PTApplication ptApplication = findApplicationById(applicationId);

    verifyTrainerAuthorization(trainer, ptApplication);

    if (ptApplication.getStatus() != PTApplicationStatus.PENDING) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
    }

    ptApplication.setStatus(PTApplicationStatus.APPROVED);

    ptApplicationRepository.save(ptApplication);

    ptContractService.createContractFromApplication(ptApplication);
  }

  @Transactional
  public void rejectApplication(Long applicationId, Long userId) {
    Member trainer = memberService.findById(userId);
    PTApplication ptApplication = findApplicationById(applicationId);

    verifyTrainerAuthorization(trainer, ptApplication);

    if (ptApplication.getStatus() != PTApplicationStatus.PENDING) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
    }

    ptApplication.setStatus(PTApplicationStatus.REJECTED);
  }

  @Transactional
  public void cancelApplication(Long applicationId, Long userId) {
    Member member = memberService.findById(userId);
    PTApplication ptApplication = findApplicationById(applicationId);

    if (!ptApplication.getMember().getId().equals(member.getId())) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }

    if (ptApplication.getStatus() != PTApplicationStatus.PENDING) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
    }

    ptApplication.setStatus(PTApplicationStatus.CANCELLED);
  }


  private PTOffering findOfferingById(Long offeringId) {
    return ptOfferingRepository.findById(offeringId)
        .orElseThrow(() -> new RestApiException(PTErrorCode.NOT_FOUND_PT_OFFERING));
  }

  private PTApplication findApplicationById(Long applicationId) {
    return ptApplicationRepository.findById(applicationId)
        .orElseThrow(
            () -> new RestApiException(PTErrorCode.NOT_FOUND_PT_APPLICATION));
  }

  private void verifyTrainerAuthorization(Member trainer, PTApplication application) {
    if (trainer.getRole() != Role.TRAINER) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }
    if (!application.getOffering().getTrainer().getId().equals(trainer.getId())) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }
  }
}
