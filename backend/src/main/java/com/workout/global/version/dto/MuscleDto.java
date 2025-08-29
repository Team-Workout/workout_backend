package com.workout.global.version.dto;

import com.workout.workout.domain.muscle.Muscle;
import lombok.Builder;

public class MuscleDto {

  @Builder
  public record SyncResponse(
      Long muscleId,
      String name,
      String koreanName,
      String muscleGroup
  ) {
    public static SyncResponse fromEntity(Muscle muscle) {
      return new SyncResponse(muscle.getId(), muscle.getName(), muscle.getKoreanName(), muscle.getMuscleGroup());
    }
  }
}
