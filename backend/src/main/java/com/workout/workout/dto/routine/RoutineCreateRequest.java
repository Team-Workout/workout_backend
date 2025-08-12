package com.workout.workout.dto.routine;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoutineCreateRequest {
  private String name;
  private String description;
  private List<RoutineExerciseDto> routineExercises;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RoutineExerciseDto {
    private Long exerciseId;
    private int order;
    private List<RoutineSetDto> routineSets;
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RoutineSetDto {
    private int order;
    private BigDecimal weight;
    private int reps;
  }
}
