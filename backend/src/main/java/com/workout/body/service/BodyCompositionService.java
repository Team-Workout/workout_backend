package com.workout.body.service;

import com.workout.body.domain.BodyComposition;
import com.workout.body.dto.BodyCompositionDto;
import com.workout.body.dto.BodyCompositionResponse;
import com.workout.body.repository.BodyCompositionRepository;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.BodyErrorCode;
import com.workout.global.exception.errorcode.MemberErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BodyCompositionService {

  private final BodyCompositionRepository bodyCompositionRepository;
  private final MemberRepository memberRepository;

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
      // 2. 데이터가 있으면, 기존 엔티티의 값을 업데이트합니다. (Update)
      log.info("기존 체성분 데이터 업데이트 - 사용자 ID: {}, 날짜: {}", memberId,
          bodyCompositionDto.getMeasurementDate());
      BodyComposition bodyCompositionToUpdate = existingData.get();

      bodyCompositionToUpdate.setWeightKg(bodyCompositionDto.getWeightKg());
      bodyCompositionToUpdate.setFatKg(bodyCompositionDto.getFatKg());
      bodyCompositionToUpdate.setMuscleMassKg(bodyCompositionDto.getMuscleMassKg());

      return bodyCompositionToUpdate.getId();

    } else {
      log.info("새로운 체성분 데이터 생성 - 사용자 ID: {}, 날짜: {}", memberId,
          bodyCompositionDto.getMeasurementDate());
      Member member = memberRepository.findById(memberId)
          .orElseThrow(() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));

      BodyComposition newBodyComposition = BodyComposition.builder()
          .member(member)
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
        .orElseThrow(() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));

    bodyComposition.setMeasurementDate(dto.getMeasurementDate());
    bodyComposition.setFatKg(dto.getFatKg());
    bodyComposition.setWeightKg(dto.getWeightKg());
    bodyComposition.setMuscleMassKg(dto.getMuscleMassKg());
  }
}
