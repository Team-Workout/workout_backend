package com.workout.global.version.dto;

import com.workout.workout.domain.exercise.ExerciseTargetMuscle;
import com.workout.workout.domain.muscle.MuscleRole;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ExerciseTargetMuscleDto {

  @Getter
  @NoArgsConstructor
  public static class MappingRequest {
    @NotNull
    private List<MappingInfo> mappings;
  }

  @Getter
  @NoArgsConstructor
  public static class MappingInfo {
    @NotNull
    private Long muscleId;
    @NotNull
    private MuscleRole muscleRole;
  }

  @Builder
  public record SyncResponse(
      Long mappingId,
      Long exerciseId,
      Long muscleId,
      MuscleRole muscleRole
  ) {
    public static SyncResponse fromEntity(ExerciseTargetMuscle mapping) {
      return new SyncResponse(
          mapping.getId(),
          mapping.getExercise().getId(),
          mapping.getTargetMuscle().getId(),
          mapping.getMuscleRole()
      );
    }
  }
}
