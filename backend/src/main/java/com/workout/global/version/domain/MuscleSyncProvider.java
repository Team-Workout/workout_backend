package com.workout.global.version.domain;

import com.workout.global.version.dto.MuscleDto;
import com.workout.workout.repository.MuscleRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MuscleSyncProvider implements DataSyncProvider {

  private final MuscleRepository muscleRepository;

  public MuscleSyncProvider(MuscleRepository muscleRepository) {
    this.muscleRepository = muscleRepository;
  }

  @Override
  public MasterDataCategory getCategory() {
    return MasterDataCategory.MUSCLE;
  }

  @Override
  public List<MuscleDto.SyncResponse> getSyncData() {
    return muscleRepository.findAll().stream()
        .map(MuscleDto.SyncResponse::fromEntity)
        .toList();
  }
}