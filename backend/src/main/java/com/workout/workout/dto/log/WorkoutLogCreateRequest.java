package com.workout.workout.dto.log;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WorkoutLogCreateRequest(
    LocalDate workoutDate,
    String logFeedback,
    List<WorkoutExerciseDto> workoutExercises
) {

  // 중첩 레코드(Nested Record) 사용
  public record WorkoutExerciseDto(
      Long exerciseId,
      int order,
      List<WorkoutSetDto> workoutSets
  ) {
  }

  // 중첩 레코드(Nested Record) 사용
  public record WorkoutSetDto(
      int order,
      BigDecimal weight,
      int reps,
      String feedback
  ) {
  }
}