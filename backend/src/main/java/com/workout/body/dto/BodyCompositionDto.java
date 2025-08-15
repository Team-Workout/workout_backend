package com.workout.body.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

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
