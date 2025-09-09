package com.workout.body.service;

import com.workout.body.domain.BodyComposition;
import com.workout.body.dto.BodyCompositionDto;
import com.workout.body.dto.BodyCompositionResponse;
import com.workout.body.repository.BodyCompositionRepository;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.BodyErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import com.workout.pt.service.contract.PTContractService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BodyCompositionService {

  private final BodyCompositionRepository bodyCompositionRepository;
  private final MemberService memberService;
  private final PTContractService pTContractService;

  public BodyCompositionService(BodyCompositionRepository bodyCompositionRepository,
      MemberService memberService, PTContractService pTContractService) {
    this.bodyCompositionRepository = bodyCompositionRepository;
    this.memberService = memberService;
    this.pTContractService = pTContractService;
  }

  public Page<BodyCompositionResponse> findByUserIdAndDateRange(Long memberId, LocalDate startDate,
      LocalDate endDate, Pageable pageable) {
    Page<BodyComposition> bodyCompositions = bodyCompositionRepository
        .findByMemberIdAndMeasurementDateBetweenOrderByMeasurementDateDesc(memberId, startDate,
            endDate, pageable);
    return bodyCompositions.map(BodyCompositionResponse::from);
  }

  @Transactional
  public void deleteBodyInfo(Long id, Long memberId) {
    BodyComposition bodyComposition = bodyCompositionRepository.findByIdAndMemberId(id, memberId)
        .orElseThrow(() -> new RestApiException(BodyErrorCode.BODY_COMPOSITION_NOT_FOUND));
    bodyCompositionRepository.delete(bodyComposition);
  }

  @Transactional
  public Long saveOrUpdateBodyComposition(BodyCompositionDto bodyCompositionDto, Long memberId) {
    Optional<BodyComposition> existingData = bodyCompositionRepository.findByMemberIdAndMeasurementDate(
        memberId, bodyCompositionDto.getMeasurementDate());

    if (existingData.isPresent()) {
      BodyComposition bodyCompositionToUpdate = existingData.get();

      bodyCompositionToUpdate.setWeightKg(bodyCompositionDto.getWeightKg());
      bodyCompositionToUpdate.setFatKg(bodyCompositionDto.getFatKg());
      bodyCompositionToUpdate.setMuscleMassKg(bodyCompositionDto.getMuscleMassKg());

      return bodyCompositionToUpdate.getId();

    } else {
      Member memberReference = memberService.getMemberReferenceById(memberId); // [변경]

      BodyComposition newBodyComposition = BodyComposition.builder()
          .member(memberReference)
          .measurementDate(bodyCompositionDto.getMeasurementDate())
          .weightKg(bodyCompositionDto.getWeightKg())
          .fatKg(bodyCompositionDto.getFatKg())
          .muscleMassKg(bodyCompositionDto.getMuscleMassKg())
          .build();

      return bodyCompositionRepository.save(newBodyComposition).getId();
    }
  }

  @Transactional
  public void updateBodyComposition(Long id, Long memberId, BodyCompositionDto dto) {
    BodyComposition bodyComposition = bodyCompositionRepository.findByIdAndMemberId(id, memberId)
        .orElseThrow(() -> new RestApiException(BodyErrorCode.BODY_COMPOSITION_NOT_FOUND));

    bodyComposition.setMeasurementDate(dto.getMeasurementDate());
    bodyComposition.setFatKg(dto.getFatKg());
    bodyComposition.setWeightKg(dto.getWeightKg());
    bodyComposition.setMuscleMassKg(dto.getMuscleMassKg());
  }

  public Page<BodyCompositionResponse> findDataByTrainer(Long trainerId, Long memberId,
      LocalDate startDate, LocalDate endDate, Pageable pageable) {
    pTContractService.validateClientBodyDataAccess(trainerId, memberId);

    Page<BodyComposition> bodyCompositions = bodyCompositionRepository
        .findByMemberIdAndMeasurementDateBetweenOrderByMeasurementDateDesc(memberId, startDate,
            endDate, pageable);

    return bodyCompositions.map(BodyCompositionResponse::from);
  }
}
