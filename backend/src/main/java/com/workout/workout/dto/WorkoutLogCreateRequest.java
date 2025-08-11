package com.workout.workout.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutLogCreateRequest {

  private LocalDate workoutDate;
  private String userMemo;
  private List<WorkoutSetDto> workoutSets;
}