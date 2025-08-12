package com.workout.workout.dto.routine;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.workout.workout.domain.routine.Routine;
import com.workout.workout.domain.routine.RoutineExercise;
import com.workout.workout.domain.routine.RoutineSet;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RoutineResponse {
  private final Long routineId;
  private final List<RoutineExerciseResponse> routineExercises;

  private RoutineResponse(Routine routine) {
    this.routineId = routine.getId();
    this.routineExercises = routine.getRoutineExercises().stream()
        .map(RoutineExerciseResponse::from)
        .collect(Collectors.toList());
  }

  @JsonCreator
  public RoutineResponse(
      @JsonProperty("routineId") Long routineId,
      @JsonProperty("routineExercises") List<RoutineExerciseResponse> routineExercises) {
    this.routineId = routineId;
    this.routineExercises = routineExercises;
  }

  public static RoutineResponse from(Routine routine) {
    return new RoutineResponse(routine);
  }

  @Getter
  public static class RoutineExerciseResponse {
    private final Long routineExerciseId;
    private final String exerciseName;
    private final int order;
    private final List<RoutineSetResponse> routineSets;

    private RoutineExerciseResponse(RoutineExercise routineExercise) {
      this.routineExerciseId = routineExercise.getId();
      this.exerciseName = routineExercise.getExercise().getName();
      this.order = routineExercise.getOrder();
      this.routineSets = routineExercise.getRoutineSets().stream()
          .map(RoutineSetResponse::from)
          .collect(Collectors.toList());
    }

    @JsonCreator
    public RoutineExerciseResponse(
        @JsonProperty("routineExerciseId") Long routineExerciseId,
        @JsonProperty("exerciseName") String exerciseName,
        @JsonProperty("order") int order,
        @JsonProperty("routineSets") List<RoutineSetResponse> routineSets) {
      this.routineExerciseId = routineExerciseId;
      this.exerciseName = exerciseName;
      this.order = order;
      this.routineSets = routineSets;
    }

    public static RoutineExerciseResponse from(RoutineExercise routineExercise) {
      return new RoutineExerciseResponse(routineExercise);
    }
  }

  @Getter
  public static class RoutineSetResponse {
    private final Long workoutSetId;
    private final int order;
    private final BigDecimal weight;
    private final int reps;

    private RoutineSetResponse(RoutineSet routineSet) {
      this.workoutSetId = routineSet.getId();
      this.order = routineSet.getOrder();
      this.weight = routineSet.getWeight();
      this.reps = routineSet.getReps();
    }

    @JsonCreator
    public RoutineSetResponse(
        @JsonProperty("workoutSetId") Long workoutSetId,
        @JsonProperty("order") int order,
        @JsonProperty("weight") BigDecimal weight,
        @JsonProperty("reps") int reps) {
      this.workoutSetId = workoutSetId;
      this.order = order;
      this.weight = weight;
      this.reps = reps;
    }

    public static RoutineSetResponse from(RoutineSet routineSet) {
      return new RoutineSetResponse(routineSet);
    }
  }
}