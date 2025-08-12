package com.workout.workout.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 이 DTO는 내부에 WorkoutExerciseDto를 리스트로 가집니다.
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutLogCreateRequest {
  private LocalDate workoutDate;
  private String logFeedback;
  private List<WorkoutExerciseDto> workoutExercises;

  // 내부 DTO: WorkoutExerciseDto는 내부에 WorkoutSetDto를 리스트로 가집니다.
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WorkoutExerciseDto {
    private Long exerciseId;
    private int logOrder;
    private List<WorkoutSetDto> workoutSets;
  }

  // 내부 DTO: WorkoutSetDto
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WorkoutSetDto {
    private int setNumber;
    private BigDecimal weight;
    private int reps;
    private String feedback;
  }
}