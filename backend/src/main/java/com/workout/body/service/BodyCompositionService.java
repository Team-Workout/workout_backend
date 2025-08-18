package com.workout.body.service;

import com.workout.body.domain.BodyComposition;
import com.workout.body.dto.BodyCompositionDto;
import com.workout.body.repository.BodyCompositionRepository;
import com.workout.member.domain.Member;
import com.workout.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BodyCompositionService {

  private final BodyCompositionRepository bodyCompositionRepository;
  private final MemberRepository memberRepository;

  public List<BodyComposition> findByUserId(Long userId) {
    List<BodyComposition> results = bodyCompositionRepository.findByUserId(userId);
    return (results == null || results.isEmpty()) ? Collections.emptyList() : results;
  }

  public void deleteBodyInfo(Long id, Long userId) {
    BodyComposition bodyComposition = bodyCompositionRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new EntityNotFoundException("Body composition NOT FOUND"));
    bodyCompositionRepository.delete(bodyComposition);
  }

  public Long createBodyComposition(BodyCompositionDto bodyCompositionDto, Long userId) {

    Member member = memberRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User NOT FOUND....ID: " + userId));

    BodyComposition bodyComposition = BodyComposition.builder()
        .member(member)
        .measurementDate(bodyCompositionDto.getMeasurementDate())
        .weightKg(bodyCompositionDto.getWeightKg())
        .fatKg(bodyCompositionDto.getFatKg())
        .muscleMassKg(bodyCompositionDto.getMuscleMassKg())
        .build();

    BodyComposition saved = bodyCompositionRepository.save(bodyComposition);
    return saved.getId();
  }

  @Transactional
  public void updateBodyComposition(Long id, BodyCompositionDto dto) {
    BodyComposition bodyComposition = bodyCompositionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("BodyInfo not found"));

    bodyComposition.setFatKg(dto.getFatKg());
    bodyComposition.setWeightKg(dto.getWeightKg());
    bodyComposition.setMuscleMassKg(dto.getMuscleMassKg());

    // Dirty Checking
  }
}
