package com.workout.pt.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.pt.domain.contract.PTApplication;
import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import com.workout.pt.domain.contract.PTOffering;
import com.workout.pt.dto.response.ContractResponse;
import com.workout.pt.repository.PTContractRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

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
      throw new IllegalStateException("이미 시작되었거나 종료된 계약은 취소할 수 없습니다.");
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
      throw new IllegalStateException("이미 시작되었거나 종료된 계약은 취소할 수 없습니다.");
    }

    contract.setStatus(PTContractStatus.CANCELLED);
    // TODO: 환불 정책에 따른 환불 로직 추가 (환불 이벤트 발행)
    ptContractRepository.save(contract);
  }

  public List<ContractResponse> getMyContracts(UserPrincipal user) {
    List<PTContract> contracts;

    // 사용자의 역할 확인
    boolean isTrainer = user.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_TRAINER"));

    if (isTrainer) {
      contracts = ptContractRepository.findAllByTrainerId(user.getUserId());
    } else { // 기본적으로 회원의 계약을 조회
      contracts = ptContractRepository.findAllByMemberId(user.getUserId());
    }

    // 조회된 엔티티 리스트를 DTO 리스트로 변환하여 반환
    return ContractResponse.from(contracts);
  }
}
