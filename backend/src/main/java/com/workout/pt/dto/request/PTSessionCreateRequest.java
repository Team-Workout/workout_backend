package com.workout.pt.dto.request;

import com.workout.workout.dto.log.WorkoutLogCreateRequest;
import jakarta.validation.constraints.NotNull;

public record PTSessionCreateRequest(
    @NotNull Long appointmentId,
    @NotNull WorkoutLogCreateRequest workoutLog
) {

}