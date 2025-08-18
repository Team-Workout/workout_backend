package com.workout.body.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
public class BodyCompositionDto {

  private Long id;

  @NotNull
  @DateTimeFormat
  private LocalDate measurementDate;

  private Long weightKg;
  private Long fatKg;
  private Long muscleMassKg;
}
