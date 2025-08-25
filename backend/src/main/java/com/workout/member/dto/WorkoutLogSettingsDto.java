package com.workout.member.dto;

import jakarta.validation.constraints.NotNull;

public record WorkoutLogSettingsDto(
    @NotNull(message = "공개 여부 값은 필수입니다.")
    Boolean isOpenWorkoutRecord
) {}