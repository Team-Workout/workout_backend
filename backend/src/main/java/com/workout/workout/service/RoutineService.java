package com.workout.workout.service;

import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.routine.Routine;
import com.workout.workout.domain.routine.RoutineExercise;
import com.workout.workout.domain.routine.RoutineSet;
import com.workout.workout.dto.routine.RoutineCreateRequest;
import com.workout.workout.dto.routine.RoutineResponse;
import com.workout.workout.dto.workOut.WorkoutLogResponse;
import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.RoutineRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RoutineService {

  private final RoutineRepository routineRepository;
  private final UserRepository userRepository;
  private final ExerciseRepository exerciseRepository;

  public RoutineService(RoutineRepository routineRepository, UserRepository userRepository, ExerciseRepository exerciseRepository) {
    this.routineRepository = routineRepository;
    this.userRepository = userRepository;
    this.exerciseRepository = exerciseRepository;
  }

  @Transactional
  public Long createRoutine(@Valid RoutineCreateRequest request, Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

    Routine routine = Routine.builder()
        .user(user)
        .name(request.getName())
        .description(request.getDescription())
        .build();

    List<Long> exerciseIds = request.getRoutineExercises().stream()
        .map(RoutineCreateRequest.RoutineExerciseDto::getExerciseId)
        .distinct()
        .toList();

    Map<Long, Exercise> exerciseMap = exerciseRepository.findAllByIdIn(exerciseIds).stream()
        .collect(Collectors.toMap(Exercise::getId, e -> e));

    request.getRoutineExercises().forEach(exerciseDto -> {
      Exercise exercise = exerciseMap.get(exerciseDto.getExerciseId());
      if(exercise == null) throw new EntityNotFoundException("운동 정보를 찾을 수 없습니다. ID: " + exerciseDto.getExerciseId());

      RoutineExercise routineExercise = RoutineExercise.builder()
          .exercise(exercise)
          .order(exerciseDto.getOrder())
          .build();
      routine.addRoutineExercise(routineExercise);

      exerciseDto.getRoutineSets().forEach(setDto -> {
        RoutineSet routineSet = RoutineSet.builder()
            .order(setDto.getOrder())
            .weight(setDto.getWeight())
            .reps(setDto.getReps())
            .build();
        routineExercise.addRoutineSet(routineSet);
      });
    });

    return routineRepository.save(routine).getId();
  }

  @Transactional
  public void deleteRoutine(Long id, Long userId) {
    Routine routine = routineRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("삭제할 루틴을 찾을 수 없습니다. ID: " + id));

    // 본인의 운동일지만 삭제 가능하도록 권한 확인
    if (!routine.getUser().getId().equals(userId)) {
      // 실제 프로젝트에서는 Custom Exception을 사용하는 것이 좋습니다.
      throw new SecurityException("해당 루틴을 삭제할 권한이 없습니다.");
    }

    routineRepository.delete(routine);
  }

  public RoutineResponse findRoutineById(Long routineId) {
    Routine routine = routineRepository.findByIdWithDetails(routineId)
        .orElseThrow(() -> new EntityNotFoundException("루틴을 찾을 수 없습니다. ID: " + routineId));
    return RoutineResponse.from(routine);
  }
}
