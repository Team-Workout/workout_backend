package com.workout.workout.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutSetDto {
  private Long exerciseId; // 어떤 운동인지 ID로 받습니다.
  private BigDecimal weight;
  private int reps;

}