package com.workout.workout.dto.routine;

import com.workout.member.domain.Member;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.routine.Routine;
import com.workout.workout.domain.routine.RoutineExercise;
import com.workout.workout.domain.routine.RoutineSet;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public record
RoutineCreateRequest(
    @NotBlank(message = "루틴 이름은 비워둘 수 없습니다.")
    String name,
    String description,
    @NotEmpty(message = "루틴에는 최소 하나 이상의 운동이 포함되어야 합니다.")
    @Valid
    List<RoutineExerciseDto> routineExercises
) {
  public Routine toEntity(Member member) {
    return Routine.builder()
        .member(member)
        .description(this.description)
        .name(this.name)
        .build();
  }


  public record RoutineExerciseDto(
      Long exerciseId,
      int order,
      @Valid
      List<RoutineSetDto> routineSets
  ) {
    public RoutineExercise toEntity(Routine routine, Exercise exercise) {
      return RoutineExercise.builder()
          .routine(routine)
          .exercise(exercise)
          .order(this.order)
          .build();
    }
  }

  public record RoutineSetDto(
      int order,
      BigDecimal weight,
      int reps
  ) {

    public RoutineSet toEntity(RoutineExercise routineExercise) {
      return RoutineSet.builder()
          .routineExercise(routineExercise)
          .order(this.order)
          .weight(this.weight)
          .reps(this.reps)
          .build();
    }
  }
}