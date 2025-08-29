package com.workout.pt.dto.response;

import com.workout.workout.dto.log.WorkoutLogResponse;

public record PTSessionResponse(
    Long id,
    WorkoutLogResponse workoutLogResponse
) {

}
