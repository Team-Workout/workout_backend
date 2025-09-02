package com.workout.global.version.dto;

import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.exercise.ExerciseTargetMuscle;
import com.workout.workout.domain.muscle.MuscleRole;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;

public class ExerciseDto {

  public record CreateRequest(@NotBlank String name) {}

  @Builder
  public record SyncResponse(
      Long exerciseId,
      String name,
      List<TargetMuscleResponse> targetMuscles
  ) {
    public static SyncResponse fromEntity(Exercise exercise) {
      return new SyncResponse(
          exercise.getId(),
          exercise.getName(),
          exercise.getMappedMuscles().stream().map(TargetMuscleResponse::fromEntity).toList()
      );
    }
  }

  @Builder
  record TargetMuscleResponse(Long muscleId, String name, MuscleRole role) {
    public static TargetMuscleResponse fromEntity(ExerciseTargetMuscle target) {
      return new TargetMuscleResponse(
          target.getTargetMuscle().getId(),
          target.getTargetMuscle().getName(),
          target.getMuscleRole()
      );
    }
  }
}