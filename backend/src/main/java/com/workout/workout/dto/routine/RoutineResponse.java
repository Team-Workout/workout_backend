package com.workout.workout.dto.routine;

import com.workout.workout.domain.routine.Routine;
import com.workout.workout.domain.routine.RoutineExercise;
import com.workout.workout.domain.routine.RoutineSet;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public record RoutineResponse(
    Long routineId,
    String routineName,
    String description,
    List<RoutineExerciseResponse> routineExercises
) {

  public static RoutineResponse from(Routine routine,
      List<RoutineExercise> exercises,
      Map<Long, List<RoutineSet>> setsMap) {

    List<RoutineExerciseResponse> exerciseResponses = exercises.stream()
        .map(exercise -> {
          // exercise ID에 해당하는 set 목록을 map에서 조회. 없으면 빈 리스트 반환.
          List<RoutineSet> sets = setsMap.getOrDefault(exercise.getId(), Collections.emptyList());
          return RoutineExerciseResponse.from(exercise, sets);
        })
        .collect(Collectors.toList());

    return new RoutineResponse(routine.getId(), routine.getName(), routine.getDescription(),
        exerciseResponses);
  }

  public record RoutineExerciseResponse(
      Long routineExerciseId,
      String exerciseName,
      int order,
      List<RoutineSetResponse> routineSets
  ) {

    public static RoutineExerciseResponse from(RoutineExercise routineExercise,
        List<RoutineSet> sets) {
      List<RoutineSetResponse> setResponses = sets.stream()
          .map(RoutineSetResponse::from)
          .collect(Collectors.toList());
      return new RoutineExerciseResponse(
          routineExercise.getId(),
          routineExercise.getExercise().getName(), // Exercise 엔티티는 LAZY 로딩이 아님을 가정
          routineExercise.getOrder(),
          setResponses
      );
    }
  }

  public record RoutineSetResponse(
      Long workoutSetId,
      int order,
      BigDecimal weight,
      int reps
  ) {

    public static RoutineSetResponse from(RoutineSet routineSet) {
      return new RoutineSetResponse(
          routineSet.getId(),
          routineSet.getOrder(),
          routineSet.getWeight(),
          routineSet.getReps()
      );
    }
  }
}