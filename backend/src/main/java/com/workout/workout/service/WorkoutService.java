package com.workout.workout.service;

import com.workout.global.version.MasterDataUpdate;
import com.workout.global.version.VersionIncrementType;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.repository.ExerciseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkoutService {

  private final ExerciseRepository exerciseRepository;

  // 간단한 이름 변경 -> PATCH 버전 업데이트
  @Transactional
  @MasterDataUpdate(category = "EXERCISE", type = VersionIncrementType.PATCH)
  public void updateExerciseName(Long exerciseId, String newName) {
  }

  // 새로운 운동 추가 (기능 추가로 간주) -> MINOR 버전 업데이트
  @Transactional
  @MasterDataUpdate(category = "EXERCISE", type = VersionIncrementType.MINOR)
  public void createNewExercise(Exercise newExercise) {
  }
}
