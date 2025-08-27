package com.workout.pt.service.contract;

import com.workout.global.exception.RestApiException;
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
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class PTContractService {

  private final PTContractRepository ptContractRepository;
  private final MemberService memberService;

  public PTContractService(PTContractRepository ptContractRepository, MemberService memberService) {
    this.ptContractRepository = ptContractRepository;
    this.memberService = memberService;
  }

  public void createContractFromApplication(PTApplication application) {

    PTOffering ptOffering = application.getOffering();

    PTContract contract = PTContract.builder().application(application) // 어떤 신청으로부터 계약이 생성되었는지 연결
        .member(application.getMember()).trainer(ptOffering.getTrainer()).gym(ptOffering.getGym())
        .price(ptOffering.getPrice()) // 신청 시의 가격 정보 사용
        .paymentDate(LocalDate.now()) //임시 로직
        .totalSessions(ptOffering.getTotalSessions()) // 신청 시의 세션 횟수
        .remainingSessions(ptOffering.getTotalSessions()) // 남은 횟수는 전체 횟수와 동일하게 시작
        .status(PTContractStatus.ACTIVE) // 계약 생성 시 상태는 '진행 중'
        .build();

    ptContractRepository.save(contract);
  }

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

    ptContractRepository.save(contract);
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

  /**
   * 회원이 PT 계약 취소
   */
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
    ptContractRepository.save(contract);
  }

  /**
   * 트레이너가 PT 계약 취소
   */
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
    ptContractRepository.save(contract);
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

  public Page<MemberResponse> getMyClients(Long userId, Pageable pageable) {
    Member member = memberService.findById(userId);

    if (member.getRole() == Role.MEMBER) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }

    Page<PTContract> contractsPage = ptContractRepository.findAllByTrainerId(
        userId, pageable);

    return contractsPage.map(contract -> MemberResponse.from(contract.getMember()));
  }

  public boolean isMyClient(Long trainerId, Long memberId) {
    return ptContractRepository.existsByTrainerIdAndMemberIdAndStatus(trainerId, memberId,
        PTContractStatus.ACTIVE);
  }
}
