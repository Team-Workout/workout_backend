package com.workout.pt.service.contract;

import com.workout.auth.domain.UserPrincipal;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;

@Service
public class PTContractService {

  private final PTContractRepository ptContractRepository;

  public PTContractService(PTContractRepository ptContractRepository) {
    this.ptContractRepository = ptContractRepository;
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
        .orElseThrow(() -> new EntityNotFoundException("계약 정보를 찾을 수 없습니다. ID: " + contractId));

    if (contract.getRemainingSessions() <= 0) {
      throw new IllegalStateException("남은 세션이 없어 차감할 수 없습니다.");
    }

    // 세션 차감
    contract.setRemainingSessions(contract.getRemainingSessions() - 1);

    // 남은 세션이 0이 되면 계약 상태를 '완료'로 변경
    if (contract.getRemainingSessions() == 0) {
      contract.setStatus(PTContractStatus.COMPLETED);
    }

    ptContractRepository.save(contract);
  }

  public void cancelContract(UserPrincipal user, Long contractId) {
    boolean isTrainer = user.getAuthorities().stream().map(GrantedAuthority::getAuthority)
        .anyMatch("ROLE_TRAINER"::equals);

    boolean isMember = user.getAuthorities().stream().map(GrantedAuthority::getAuthority)
        .anyMatch("ROLE_MEMBER"::equals);

    if (isTrainer) {
      cancelContractByTrainer(user, contractId);
    } else if (isMember) {
      cancelContractByMember(user, contractId);
    } else {
      throw new AccessDeniedException("계약을 취소할 권한이 없습니다.");
    }
  }

  /**
   * 회원이 PT 계약 취소
   */
  public void cancelContractByMember(UserPrincipal member, Long contractId) {
    PTContract contract = ptContractRepository.findById(contractId)
        .orElseThrow(() -> new EntityNotFoundException("계약 정보를 찾을 수 없습니다. ID: " + contractId));

    // 본인의 계약이 맞는지 확인
    if (!contract.getMember().getId().equals(member.getUserId())) {
      throw new AccessDeniedException("계약을 취소할 권한이 없습니다.");
    }

    // 이미 진행중인 계약만 취소 가능하도록 비즈니스 규칙 설정
    if (contract.getStatus() != PTContractStatus.ACTIVE) {
      ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
          "이미 시작되었거나 종료된 계약은 취소할 수 없습니다.");
      throw new ErrorResponseException(HttpStatus.BAD_REQUEST, pd, null);
    }

    contract.setStatus(PTContractStatus.CANCELLED);
    // TODO: 환불 정책에 따른 환불 로직 추가 (환불 이벤트 발행)
    ptContractRepository.save(contract);
  }

  /**
   * 트레이너가 PT 계약 취소
   */
  public void cancelContractByTrainer(UserPrincipal trainer, Long contractId) {
    PTContract contract = ptContractRepository.findById(contractId)
        .orElseThrow(() -> new EntityNotFoundException("계약 정보를 찾을 수 없습니다. ID: " + contractId));

    // 본인의 계약이 맞는지 확인
    if (!contract.getTrainer().getId().equals(trainer.getUserId())) {
      throw new AccessDeniedException("계약을 취소할 권한이 없습니다.");
    }

    // 이미 진행중인 계약만 취소 가능하도록 비즈니스 규칙 설정
    if (contract.getStatus() != PTContractStatus.ACTIVE) {
      ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
          "이미 시작되었거나 종료된 계약은 취소할 수 없습니다.");
      throw new ErrorResponseException(HttpStatus.BAD_REQUEST, pd, null);
    }

    contract.setStatus(PTContractStatus.CANCELLED);
    // TODO: 환불 정책에 따른 환불 로직 추가 (환불 이벤트 발행)
    ptContractRepository.save(contract);
  }

  public Page<ContractResponse> getMyContracts(UserPrincipal user, Pageable pageable) {
    Page<PTContract> contractsPage;

    boolean isTrainer = user.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_TRAINER"));

    if (isTrainer) {
      contractsPage = ptContractRepository.findAllByTrainerId(user.getUserId(), pageable);
    } else {
      contractsPage = ptContractRepository.findAllByMemberId(user.getUserId(), pageable);
    }

    Page<ContractResponse> dtoPage = contractsPage.map(ContractResponse::from);

    return dtoPage;
  }

  public Page<MemberResponse> getMyClients(UserPrincipal trainerUser, Pageable pageable) {
    boolean isTrainer = trainerUser.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_TRAINER"));

    if (!isTrainer) {
      throw new AccessDeniedException("트레이너만 클라이언트 목록을 조회할 수 있습니다.");
    }

    Page<PTContract> contractsPage = ptContractRepository.findAllByTrainerId(
        trainerUser.getUserId(), pageable);

    return contractsPage.map(contract -> MemberResponse.from(contract.getMember()));
  }
}
