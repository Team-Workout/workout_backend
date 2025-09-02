package com.workout.global.version.domain;

import com.workout.global.version.dto.ExerciseTargetMuscleDto;
import com.workout.global.version.dto.ExerciseTargetMuscleDto.SyncResponse;
import com.workout.workout.repository.ExerciseTargetMuscleRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ExerciseTargetMuscleSyncProvider implements DataSyncProvider {

  private final ExerciseTargetMuscleRepository repository;

  public ExerciseTargetMuscleSyncProvider(ExerciseTargetMuscleRepository repository) {
    this.repository = repository;
  }

  @Override
  public MasterDataCategory getCategory() {
    return MasterDataCategory.EXERCISE_TARGET_MUSCLE;
  }

  @Override
  public List<SyncResponse> getSyncData() {
    return repository.findAll().stream()
        .map(ExerciseTargetMuscleDto.SyncResponse::fromEntity)
        .toList();
  }
}