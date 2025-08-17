package com.workout.workout.dto.log;

import com.workout.member.domain.Member;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.log.WorkoutExercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutSet;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.validator.constraints.NotEmpty;

public record WorkoutLogCreateRequest(
    LocalDate workoutDate,
    String logFeedback,
    @NotEmpty(message = "운동일지에는 최소 하나 이상의 운동이 포함되어야 합니다.")
    @Valid
    List<WorkoutExerciseDto> workoutExercises
) {

  public WorkoutLog toEntity(Member member) {
    return WorkoutLog.builder()
        .member(member)
        .workoutDate(this.workoutDate)
        .build();
  }

  public record WorkoutExerciseDto(
      Long exerciseId,
      int order,
      List<WorkoutSetDto> workoutSets
  ) {

    public WorkoutExercise toEntity(WorkoutLog workoutLog, Exercise exercise) {
      return WorkoutExercise.builder()
          .workoutLog(workoutLog)
          .exercise(exercise)
          .order(this.order)
          .build();
    }
  }

  public record WorkoutSetDto(
      int order,
      BigDecimal weight,
      int reps,
      String feedback
  ) {

    public WorkoutSet toEntity(WorkoutExercise workoutExercise) {
      return WorkoutSet.builder()
          .workoutExercise(workoutExercise)
          .order(this.order)
          .weight(this.weight)
          .reps(this.reps)
          .build();
    }
  }
}