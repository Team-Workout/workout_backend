package com.workout.workout.dto;

import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutSet;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class WorkoutLogResponse {

  private final Long workoutLogId;
  private final LocalDate workoutDate;
  private final String userMemo;
  private final List<WorkoutSetResponse> workoutSets;

  public WorkoutLogResponse(WorkoutLog workoutLog) {
    this.workoutLogId = workoutLog.getId();
    this.workoutDate = workoutLog.getWorkoutDate();
    this.userMemo = workoutLog.getUserMemo();
    this.workoutSets = workoutLog.getWorkoutSets().stream()
        .map(WorkoutSetResponse::new)
        .collect(Collectors.toList());
  }

  @Getter
  public static class WorkoutSetResponse {
    private final Long workoutSetId;
    private final String exerciseName;
    private final int setNumber;
    private final BigDecimal weight;
    private final int reps;

    public WorkoutSetResponse(WorkoutSet workoutSet) {
      this.workoutSetId = workoutSet.getId();
      this.exerciseName = workoutSet.getExercise().getName(); // 운동 이름을 포함
      this.setNumber = workoutSet.getSetNumber();
      this.weight = workoutSet.getWeight();
      this.reps = workoutSet.getReps();
    }
  }
}