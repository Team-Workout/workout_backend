package com.workout.body.dto;

import com.workout.body.domain.BodyComposition;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BodyCompositionResponse(
    Long id,
    LocalDate measurementDate,
    BigDecimal weightKg,
    BigDecimal fatKg,
    BigDecimal muscleMassKg,
    Long memberId
) {
  public static BodyCompositionResponse from(BodyComposition bodyComposition) {
    return new BodyCompositionResponse(
        bodyComposition.getId(),
        bodyComposition.getMeasurementDate(),
        bodyComposition.getWeightKg(),
        bodyComposition.getFatKg(),
        bodyComposition.getMuscleMassKg(),
        bodyComposition.getMember().getId()
    );
  }

}
