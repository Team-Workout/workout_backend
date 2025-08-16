package com.workout.workout.service;

import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.routine.Routine;
import com.workout.workout.domain.routine.RoutineExercise;
import com.workout.workout.domain.routine.RoutineSet;
import com.workout.workout.dto.routine.RoutineCreateRequest;
import com.workout.workout.dto.routine.RoutineResponse;
import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.routine.RoutineExerciseRepository;
import com.workout.workout.repository.routine.RoutineRepository;
import com.workout.workout.repository.routine.RoutineSetRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoutineService {

  private final RoutineRepository routineRepository;
  private final UserRepository userRepository;
  private final ExerciseRepository exerciseRepository;
  private final RoutineExerciseRepository routineExerciseRepository;
  private final RoutineSetRepository routineSetRepository;

  public RoutineService(
      RoutineRepository routineRepository, UserRepository userRepository,
      ExerciseRepository exerciseRepository, RoutineExerciseRepository routineExerciseRepository,
      RoutineSetRepository routineSetRepository) {

    this.routineRepository = routineRepository;
    this.userRepository = userRepository;
    this.exerciseRepository = exerciseRepository;
    this.routineExerciseRepository = routineExerciseRepository;
    this.routineSetRepository = routineSetRepository;
  }

  @Transactional
  public Long createRoutine(@Valid RoutineCreateRequest request, Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

    // 부모 엔티티 생성 및 저장
    Routine routine = request.toEntity(user);
    routineRepository.save(routine);

    List<Long> exerciseIds = request.routineExercises().stream()
        .map(RoutineCreateRequest.RoutineExerciseDto::exerciseId)
        .distinct()
        .collect(Collectors.toList());

    Map<Long, Exercise> exerciseMap = exerciseRepository.findAllByIdIn(exerciseIds).stream()
        .collect(Collectors.toMap(Exercise::getId, e -> e));

    List<RoutineExercise> routineExercisesToSave = new ArrayList<>();
    List<RoutineSet> routineSetsToSave = new ArrayList<>();

    // 자식, 자손 엔티티 생성 (DB에 저장 전)
    request.routineExercises().forEach(exerciseDto -> {
      Exercise exercise = exerciseMap.get(exerciseDto.exerciseId());
      if (exercise == null) {
        throw new EntityNotFoundException("운동 정보를 찾을 수 없습니다. ID: " + exerciseDto.exerciseId());
      }

      RoutineExercise routineExercise = exerciseDto.toEntity(routine, exercise);
      routineExercisesToSave.add(routineExercise);

      exerciseDto.routineSets().forEach(setDto -> {
        RoutineSet routineSet = setDto.toEntity(routineExercise);
        routineSetsToSave.add(routineSet);
      });
    });

    // 자식, 자손 엔티티를 Bulk Insert로 한번에 저장 (성능 최적화)
    routineExerciseRepository.saveAll(routineExercisesToSave);
    routineSetRepository.saveAll(routineSetsToSave);

    return routine.getId();
  }

  @Transactional
  public void deleteRoutine(Long id, Long userId) {
    Routine routine = routineRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("삭제할 루틴을 찾을 수 없습니다. ID: " + id));

    if (!routine.getUser().getId().equals(userId)) {
      throw new SecurityException("해당 루틴을 삭제할 권한이 없습니다.");
    }

    List<RoutineExercise> routineExercises = routineExerciseRepository.findAllByRoutineIdOrderByOrderAsc(id);
    if (routineExercises.isEmpty()) {
      routineRepository.delete(routine);
      return;
    }
    List<Long> routineExerciseIds = routineExercises.stream().map(RoutineExercise::getId).toList();

    routineSetRepository.deleteAllByRoutineExerciseIdIn(routineExerciseIds);
    routineExerciseRepository.deleteAllByRoutineId(id);
    routineRepository.delete(routine);
  }

  public RoutineResponse findRoutineById(Long routineId) {
    // 부모(Routine) 조회
    Routine routine = routineRepository.findById(routineId)
        .orElseThrow(() -> new EntityNotFoundException("루틴을 찾을 수 없습니다. ID: " + routineId));

    // 자식(RoutineExercise) 목록 조회
    List<RoutineExercise> routineExercises = routineExerciseRepository.findAllByRoutineIdOrderByOrderAsc(routineId);

    // 자손(RoutineSet) 목록을 한 번의 쿼리로 조회 후 맵으로 그룹핑
    Map<Long, List<RoutineSet>> routineSetsMap;
    if (routineExercises.isEmpty()) {
      routineSetsMap = new HashMap<>();
    } else {
      List<Long> routineExerciseIds = routineExercises.stream()
          .map(RoutineExercise::getId)
          .toList();
      List<RoutineSet> routineSets = routineSetRepository.findAllByRoutineExerciseIdInOrderByOrderAsc(routineExerciseIds);
      routineSetsMap = routineSets.stream()
          .collect(Collectors.groupingBy(rs -> rs.getRoutineExercise().getId()));
    }

    // 서비스 계층에서 DTO로 조립하여 반환
    return RoutineResponse.from(routine, routineExercises, routineSetsMap);
  }
}