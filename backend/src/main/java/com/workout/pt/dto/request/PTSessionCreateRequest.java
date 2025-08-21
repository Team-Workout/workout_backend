package com.workout.pt.dto.request;

import jakarta.validation.constraints.NotNull;

public record PTSessionCreateRequest(
    @NotNull Long workoutLogId,
    @NotNull Long appointmentId
) {
}