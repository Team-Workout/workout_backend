package com.workout.pt.service.contract;

import com.workout.body.dto.BodyCompositionResponse;
import com.workout.body.service.BodyCompositionService;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.BodyErrorCode;
import com.workout.global.exception.errorcode.MemberErrorCode;
import com.workout.global.exception.errorcode.PTErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.service.MemberService;
import com.workout.pt.domain.contract.PTApplication;
import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import com.workout.pt.domain.contract.PTOffering;
import com.workout.pt.dto.response.ClientListResponse.MemberResponse;
import com.workout.pt.dto.response.ContractResponse;
import com.workout.pt.repository.PTContractRepository;
import com.workout.utils.domain.UserFile;
import com.workout.utils.dto.FileResponse;
import com.workout.utils.service.FileService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PTContractService {

  private final PTContractRepository ptContractRepository;
  private final MemberService memberService;
  private final FileService fileService;

  public PTContractService(PTContractRepository ptContractRepository, MemberService memberService,
      FileService fileService) {
    this.fileService = fileService;
    this.ptContractRepository = ptContractRepository;
    this.memberService = memberService;
  }

  public void createContractFromApplication(PTApplication application) {

    PTOffering ptOffering = application.getOffering();

    PTContract contract = PTContract.builder().application(application)
        .member(application.getMember()).trainer(ptOffering.getTrainer()).gym(ptOffering.getGym())
        .price(ptOffering.getPrice())
        .paymentDate(LocalDate.now())
        .totalSessions(ptOffering.getTotalSessions())
        .remainingSessions(ptOffering.getTotalSessions())
        .status(PTContractStatus.ACTIVE)
        .build();
  }

  @Transactional
  public void deductSession(Long contractId) {
    PTContract contract = ptContractRepository.findById(contractId)
        .orElseThrow(() -> new RestApiException(PTErrorCode.NOT_FOUND_PT_CONTRACT));

    if (contract.getRemainingSessions() <= 0) {
      throw new RestApiException(PTErrorCode.NO_REMAIN_SESSION);
    }

    // 세션 차감
    contract.setRemainingSessions(contract.getRemainingSessions() - 1);

    // 남은 세션이 0이 되면 계약 상태를 '완료'로 변경
    if (contract.getRemainingSessions() == 0) {
      contract.setStatus(PTContractStatus.COMPLETED);
    }
  }

  public void cancelContract(Long userId, Long contractId) {
    Member member = memberService.findById(userId);

    if (member.getRole() == Role.TRAINER) {
      cancelContractByTrainer(userId, contractId);
    } else if (member.getRole() == Role.MEMBER) {
      cancelContractByMember(userId, contractId);
    } else {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }
  }

  @Transactional
  public void cancelContractByMember(Long userId, Long contractId) {
    PTContract contract = ptContractRepository.findById(contractId)
        .orElseThrow(() -> new EntityNotFoundException("계약 정보를 찾을 수 없습니다. ID: " + contractId));

    // 본인의 계약이 맞는지 확인
    if (!contract.getMember().getId().equals(userId)) {
      throw new AccessDeniedException("계약을 취소할 권한이 없습니다.");
    }

    // 이미 진행중인 계약만 취소 가능하도록 비즈니스 규칙 설정
    if (contract.getStatus() != PTContractStatus.ACTIVE) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
    }

    contract.setStatus(PTContractStatus.CANCELLED);
    // TODO: 환불 정책에 따른 환불 로직 추가 (환불 이벤트 발행)
  }

  @Transactional
  public void cancelContractByTrainer(Long userId, Long contractId) {
    PTContract contract = ptContractRepository.findById(contractId)
        .orElseThrow(() -> new RestApiException(PTErrorCode.NOT_FOUND_PT_CONTRACT));

    // 본인의 계약이 맞는지 확인
    if (!contract.getTrainer().getId().equals(userId)) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }

    // 이미 진행중인 계약만 취소 가능하도록 비즈니스 규칙 설정
    if (contract.getStatus() != PTContractStatus.ACTIVE) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
    }

    contract.setStatus(PTContractStatus.CANCELLED);
    // TODO: 환불 정책에 따른 환불 로직 추가 (환불 이벤트 발행)
  }

  public Page<ContractResponse> getMyContracts(Long userId, Pageable pageable) {
    Page<PTContract> contractsPage;

    Member member = memberService.findById(userId);

    if (member.getRole() == Role.TRAINER) {
      contractsPage = ptContractRepository.findAllByTrainerId(userId, pageable);
    } else {
      contractsPage = ptContractRepository.findAllByMemberId(userId, pageable);
    }

    Page<ContractResponse> dtoPage = contractsPage.map(ContractResponse::from);

    return dtoPage;
  }

  public boolean isMyClient(Long trainerId, Long memberId) {
    return ptContractRepository.existsByTrainerIdAndMemberIdAndStatus(trainerId, memberId,
        PTContractStatus.ACTIVE);
  }

  public Page<MemberResponse> findMyClients(Long trainerId, Pageable pageable) {
    Page<PTContract> contractsPage = ptContractRepository
        .findByTrainerIdAndStatus(trainerId, PTContractStatus.ACTIVE, pageable);

    return contractsPage.map(contract -> MemberResponse.from(contract.getMember()));
  }

  public void validateClientBodyDataAccess(Long trainerId, Long memberId) {
    if (!this.isMyClient(trainerId, memberId)) {
      throw new RestApiException(MemberErrorCode.NOT_YOUR_CLIENT);
    }

    Member member = memberService.findById(memberId);

    if (!member.getIsOpenBodyComposition()) {
      throw new RestApiException(BodyErrorCode.NOT_ALLOWED);
    }
  }

  public void validateClientBodyImgAccess(Long trainerId, Long memberId) {
    if (!this.isMyClient(trainerId, memberId)) {
      throw new RestApiException(MemberErrorCode.NOT_YOUR_CLIENT);
    }

    Member member = memberService.findById(memberId);

    if (!member.getIsOpenBodyImg()) {
      throw new RestApiException(BodyErrorCode.NOT_ALLOWED);
    }
  }

  public void validateClientWokrOutDataAccess(Long trainerId, Long memberId) {
    if (!this.isMyClient(trainerId, memberId)) {
      throw new RestApiException(MemberErrorCode.NOT_YOUR_CLIENT);
    }

    Member member = memberService.findById(memberId);

    if (!member.getIsOpenWorkoutRecord()) {
      throw new RestApiException(BodyErrorCode.NOT_ALLOWED);
    }
  }

  public Page<FileResponse> findMemberBodyImagesByTrainer(Long trainerId, Long memberId,
      LocalDate startDate, LocalDate endDate, Pageable pageable) {

    validateClientBodyImgAccess(trainerId, memberId); // (내부 호출로 변경)

    Page<UserFile> userFilesPage = fileService.findBodyImagesByMember(
        memberId, startDate, endDate, pageable);

    return userFilesPage.map(FileResponse::from);
  }

  @Transactional
  public void terminateAllContractsForTrainer(Long trainerId) {
    List<PTContract> contracts = ptContractRepository.findAllByTrainerId(trainerId);
    ptContractRepository.deleteAll(contracts);
  }
}
