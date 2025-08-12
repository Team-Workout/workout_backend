package com.workout.workout.dto.routine;

import com.workout.workout.domain.log.Feedback;
import com.workout.workout.domain.log.WorkoutExercise;
import com.workout.workout.domain.log.WorkoutSet;
import com.workout.workout.domain.routine.Routine;
import com.workout.workout.domain.routine.RoutineExercise;
import com.workout.workout.domain.routine.RoutineSet;
import com.workout.workout.dto.workOut.WorkoutLogResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class RoutineResponse {
  private final Long routineId;
  private final List<RoutineExerciseResponse> routineExercises; // [수정] workoutSets -> workoutExercises

  private RoutineResponse(Routine routine) {
    this.routineId = routine.getId();
    this.routineExercises = routine.getRoutineExercises().stream()
        .map(RoutineExerciseResponse::from)
        .collect(Collectors.toList());
  }

  public static RoutineResponse from(Routine routine) {
    return new RoutineResponse(routine);
  }

  @Getter
  public static class RoutineExerciseResponse  { // [신규] 운동 그룹을 위한 응답 DTO
    private final Long routineExerciseId;
    private final String exerciseName;
    private final int order;
    private final List<RoutineSetResponse> routineSets;

    private RoutineExerciseResponse (RoutineExercise routineExercise) {
      this.routineExerciseId = routineExercise.getId();
      this.exerciseName = routineExercise.getExercise().getName();
      this.order = routineExercise.getOrder();
      this.routineSets = routineExercise.getRoutineSets().stream()
          .map(RoutineSetResponse::from)
          .collect(Collectors.toList());
    }

    public static RoutineExerciseResponse  from(RoutineExercise routineExercise) {
      return new RoutineExerciseResponse (routineExercise);
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

    public static RoutineSetResponse from(RoutineSet routineSet) {
      return new RoutineSetResponse(routineSet);
    }
  }

}
