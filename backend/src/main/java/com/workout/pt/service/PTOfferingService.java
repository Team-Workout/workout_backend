package com.workout.pt.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import com.workout.pt.domain.contract.PTOffering;
import com.workout.pt.domain.contract.PTOfferingStatus;
import com.workout.pt.dto.request.OfferingCreateRequest;
import com.workout.pt.dto.response.PtOfferingResponse;
import com.workout.pt.repository.PTOfferingRepository;
import com.workout.trainer.domain.Trainer;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class PTOfferingService {

  private final PTOfferingRepository ptOfferingRepository;
  private final MemberRepository memberRepository;

  public PTOfferingService(PTOfferingRepository ptOfferingRepository,
      MemberRepository memberRepository) {
    this.ptOfferingRepository = ptOfferingRepository;
    this.memberRepository = memberRepository;
  }

  public void register(OfferingCreateRequest request, UserPrincipal userPrincipal) {
    // 1. UserPrincipal을 통해 트레이너 정보를 조회
    Member user = memberRepository.findById(userPrincipal.getUserId())
        .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));

    // 2. 트레이너 역할인지 확인
    if (user.getRole() != Role.TRAINER) {
      throw new AccessDeniedException("트레이너만 PT 상품을 등록할 수 있습니다.");
    }

    // 3. DTO를 Entity로 변환하여 저장
    PTOffering offering = PTOffering.builder()
        .trainer((Trainer) user) // Member를 Trainer로 캐스팅
        .title(request.title())
        .gym(user.getGym())
        .description(request.description())
        .price(request.price())
        .totalSessions(request.totalSessions())
        .status(PTOfferingStatus.ACTIVE)
        .build();

    ptOfferingRepository.save(offering);
  }

  public void delete(Long offeringId, UserPrincipal userPrincipal) {
    Member member = memberRepository.findById(userPrincipal.getUserId())
        .orElseThrow(() -> new EntityNotFoundException(
            "트레이너 정보를 찾을 수 없습니다. ID: " + userPrincipal.getUserId()));
    PTOffering ptOffering = ptOfferingRepository.findById(offeringId).orElseThrow(
        () -> new EntityNotFoundException("pt offering 정보를 찾을 수 없습니다. ID: " + offeringId));

    if (!ptOffering.getTrainer().getId().equals(member.getId())) {
      throw new AccessDeniedException("오퍼를 지울 권한이 없습니다.");
    }

    ptOfferingRepository.delete(ptOffering);
  }

  public PtOfferingResponse findbyTrainerId(Long trainerId) {
    memberRepository.findById(trainerId)
        .orElseThrow(() -> new EntityNotFoundException("트레이너 정보를 찾을 수 없습니다. ID: " + trainerId));

    return PtOfferingResponse.from(ptOfferingRepository.findAllByTrainerId(trainerId));
  }
}
