/*
package com.workout.global.version.service;

import com.workout.global.version.MasterDataUpdate;
import com.workout.global.version.domain.MasterDataCategory;
import com.workout.global.version.dto.ExerciseAdminDto;
import com.workout.global.version.dto.ExerciseAdminDto.ExerciseUpdateItem;
import com.workout.global.version.dto.ExerciseAdminDto.TargetMuscleItem;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.exercise.ExerciseTargetMuscle;
import com.workout.workout.domain.muscle.Muscle;

import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.ExerciseTargetMuscleRepository;
import com.workout.workout.repository.MuscleRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseAdminService {

  private final ExerciseRepository exerciseRepository;
  private final MuscleRepository muscleRepository; // Muscle 엔티티 조회를 위해 추가
  private final ExerciseTargetMuscleRepository targetMuscleRepository; // 관계 테이블 관리를 위해 추가

  // [중요] 두 종류의 마스터 데이터가 변경되므로, 각각에 대한 버전을 업데이트해야 합니다.
  // 현재 구조에서는 하나의 메소드에 하나의 어노테이션만 가능하므로, 서비스 로직을 분리하거나
  // AOP 로직을 더 고도화해야 하지만, 여기서는 대표로 EXERCISE 버전을 올리겠습니다.
  @MasterDataUpdate(category = MasterDataCategory.EXERCISE)
  @Transactional
  public void bulkUpdateExercises(ExerciseAdminDto.BulkUpdateRequest request) {

    // --- 1. 사전 데이터 준비 (성능 최적화) ---
    // 업데이트/삭제할 기존 운동들을 ID로 한 번에 조회
    List<Long> existingIds = request.exercises().stream()
        .filter(item -> item.id() != null)
        .map(ExerciseUpdateItem::id)
        .toList();
    Map<Long, Exercise> existingExercisesMap = exerciseRepository.findAllById(existingIds).stream()
        .collect(Collectors.toMap(Exercise::getId, Function.identity()));

    // 요청에 포함된 모든 Muscle ID를 추출하여 한 번에 조회
    List<Long> allMuscleIds = request.exercises().stream()
        .flatMap(ex -> ex.targetMuscles().stream())
        .map(TargetMuscleItem::muscleId)
        .distinct()
        .toList();
    Map<Long, Muscle> muscleMap = muscleRepository.findAllById(allMuscleIds).stream()
        .collect(Collectors.toMap(Muscle::getId, Function.identity()));


    // --- 2. 요청 목록을 순회하며 상태에 따라 분기 처리 ---
    for (ExerciseUpdateItem item : request.exercises()) {
      switch (item.status()) {
        case CREATED:
          Exercise newExercise = Exercise.builder().name(item.name()).build();
          exerciseRepository.save(newExercise);
          // 새로 생성된 운동에 대한 근육 관계 설정
          syncMuscleMappings(newExercise, item.targetMuscles(), muscleMap);
          break;
        case UPDATED:
          Exercise exerciseToUpdate = existingExercisesMap.get(item.id());
          if (exerciseToUpdate != null) {
            exerciseToUpdate.updateName(item.name()); // 이름 등 부가 정보 업데이트
            // 기존 운동에 대한 근육 관계 '전체 동기화'
            syncMuscleMappings(exerciseToUpdate, item.targetMuscles(), muscleMap);
          }
          break;
        case DELETED:
          if (existingExercisesMap.containsKey(item.id())) {
            // Exercise에 CascadeType.ALL, orphanRemoval=true 설정이 되어있다면
            // exerciseRepository.deleteById() 만으로도 연관된 ExerciseTargetMuscle이 함께 삭제됩니다.
            // 그렇지 않다면, targetMuscleRepository에서 먼저 삭제해야 합니다.
            targetMuscleRepository.deleteAllByExerciseId(item.id());
            exerciseRepository.deleteById(item.id()); // Soft Delete 실행
          }
          break;
      }
    }
  }

  */
/**
   * 특정 운동에 대한 타겟 근육 관계를 동기화하는 private 메소드 (Delete-All and Re-create 전략)
   * @param exercise 기준이 되는 운동 엔티티
   * @param muscleItems DTO로부터 받은 새로운 근육 관계 목록
   * @param muscleMap 미리 조회해둔 전체 근육 Map
   *//*

  private void syncMuscleMappings(Exercise exercise, List<TargetMuscleItem> muscleItems, Map<Long, Muscle> muscleMap) {
    // 1. 기존의 모든 근육 관계를 삭제
    targetMuscleRepository.deleteAllByExerciseId(exercise.getId());

    // 2. DTO에 명시된 새로운 근육 관계 목록을 생성
    if (muscleItems == null || muscleItems.isEmpty()) {
      return; // 연결할 근육이 없으면 여기서 종료
    }

    List<ExerciseTargetMuscle> newMappings = muscleItems.stream()
        .map(item -> {
          Muscle muscle = muscleMap.get(item.muscleId());
          if (muscle == null) {
            throw new IllegalArgumentException("Muscle not found for id: " + item.muscleId());
          }
          return ExerciseTargetMuscle.builder()
              .exercise(exercise)
              .targetMuscle(muscle)
              .muscleRole(item.muscleRole())
              .build();
        })
        .toList();

    // 3. 새로운 관계 목록을 DB에 한 번에 저장
    targetMuscleRepository.saveAll(newMappings);
  }
}*/
