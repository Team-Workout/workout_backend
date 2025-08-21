package com.workout.pt.service.contract;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import com.workout.pt.dto.response.ClientListResponse.MemberResponse;
import com.workout.pt.repository.PTContractRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class PTTrainerService {

  private final MemberRepository memberRepository;
  private final PTContractRepository ptContractRepository;

  public PTTrainerService(MemberRepository memberRepository,
      PTContractRepository ptContractRepository) {
    this.memberRepository = memberRepository;
    this.ptContractRepository = ptContractRepository;
  }

  public Page<MemberResponse> findMyClients(UserPrincipal trainerPrincipal, Pageable pageable) {
    // 1. 사용자 조회 및 트레이너 역할 검증
    Member trainer = memberRepository.findById(trainerPrincipal.getUserId())
        .orElseThrow(() -> new EntityNotFoundException(
            "트레이너를 찾을 수 없습니다. ID: " + trainerPrincipal.getUserId()));

    if (trainer.getRole() != Role.TRAINER) {
      throw new AccessDeniedException("트레이너만 이용할 수 있는 서비스입니다.");
    }

    // 2. 트레이너의 활성 계약 목록을 페이징하여 조회
    Page<PTContract> contractsPage = ptContractRepository
        .findByTrainerIdAndStatus(trainer.getId(), PTContractStatus.ACTIVE, pageable);

    // 3. Page<PTContract>를 Page<MemberResponse>로 직접 변환하여 반환
    // .distinct()가 필요 없어졌고, 페이징 정보가 그대로 유지됩니다.
    return contractsPage.map(contract -> MemberResponse.from(contract.getMember()));
  }
}
