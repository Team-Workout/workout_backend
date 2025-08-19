package com.workout.pt.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import com.workout.pt.dto.response.ClientListResponse;
import com.workout.pt.repository.PTContractRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
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

  public ClientListResponse findMyClients(UserPrincipal trainerPrincipal) {
    Member trainer = memberRepository.findById(trainerPrincipal.getUserId())
        .orElseThrow(() -> new EntityNotFoundException(
            "트레이너를 찾을 수 없습니다. ID: " + trainerPrincipal.getUserId()));

    if (trainer.getRole() != Role.TRAINER) {
      throw new AccessDeniedException("트레이너만 이용할 수 있는 서비스입니다.");
    }

    List<PTContract> activeContracts = ptContractRepository
        .findByTrainerIdAndStatus(trainer.getId(), PTContractStatus.ACTIVE);

    List<Member> clients = activeContracts.stream()
        .map(PTContract::getMember)
        .distinct()
        .collect(Collectors.toList());

    return ClientListResponse.from(clients);
  }
}
