package com.workout.body.dto;

import com.workout.body.domain.BodyComposition;
import java.time.LocalDate;

public record BodyCompositionResponse(
    Long id,
    LocalDate measurementDate,
    Long weightKg,
    Long fatKg,
    Long muscleMassKg,
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
