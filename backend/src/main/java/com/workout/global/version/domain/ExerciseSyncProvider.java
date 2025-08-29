package com.workout.global.version.domain;

import com.workout.global.version.dto.ExerciseDto;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.repository.ExerciseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class ExerciseSyncProvider implements DataSyncProvider {

  private final ExerciseRepository exerciseRepository;

  public ExerciseSyncProvider(ExerciseRepository exerciseRepository) {
    this.exerciseRepository = exerciseRepository;
  }

  @Override
  public MasterDataCategory getCategory() {
    return MasterDataCategory.EXERCISE;
  }

  @Override
  public List<ExerciseDto.SyncResponse> getSyncData() {
    return exerciseRepository.findAllWithMuscles().stream()
        .map(ExerciseDto.SyncResponse::fromEntity)
        .toList();
  }
}