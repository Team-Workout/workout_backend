package com.workout.workout.service;

import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.log.Feedback;
import com.workout.workout.domain.log.WorkoutExercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutSet;
import com.workout.workout.dto.WorkoutLogCreateRequest;
import com.workout.workout.dto.WorkoutLogResponse;
import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.WorkoutLogRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션을 사용
public class WorkoutLogService {

  private final WorkoutLogRepository workoutLogRepository;
  private final UserRepository userRepository;
  private final ExerciseRepository exerciseRepository;

  public WorkoutLogService(WorkoutLogRepository workoutLogRepository, UserRepository userRepository, ExerciseRepository exerciseRepository) {
    this.workoutLogRepository = workoutLogRepository;
    this.userRepository = userRepository;
    this.exerciseRepository = exerciseRepository;
  }

  @Transactional
  public Long createWorkoutLog(WorkoutLogCreateRequest request, Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

    WorkoutLog workoutLog = WorkoutLog.builder()
        .user(user)
        .workoutDate(request.getWorkoutDate())
        .build();

    List<Long> exerciseIds = request.getWorkoutExercises().stream()
        .map(WorkoutLogCreateRequest.WorkoutExerciseDto::getExerciseId)
        .distinct()
        .toList();

    Map<Long, Exercise> exerciseMap = exerciseRepository.findAllByIdIn(exerciseIds).stream()
        .collect(Collectors.toMap(Exercise::getId, e -> e));

    request.getWorkoutExercises().forEach(exerciseDto -> {
      Exercise exercise = exerciseMap.get(exerciseDto.getExerciseId());
      if (exercise == null) throw new EntityNotFoundException("운동 정보를 찾을 수 없습니다. ID: " + exerciseDto.getExerciseId());

      WorkoutExercise workoutExercise = WorkoutExercise.builder()
          .exercise(exercise)
          .logOrder(exerciseDto.getLogOrder())
          .build();
      workoutLog.addWorkoutExercise(workoutExercise);

      exerciseDto.getWorkoutSets().forEach(setDto -> {
        WorkoutSet workoutSet = WorkoutSet.builder()
            .setNumber(setDto.getSetNumber())
            .weight(setDto.getWeight())
            .reps(setDto.getReps())
            .build();
        workoutExercise.addWorkoutSet(workoutSet);

        if (setDto.getFeedback() != null && !setDto.getFeedback().isBlank()) {
          Feedback feedback = Feedback.builder()
              .author(user) // 피드백 작성자는 현재 사용자
              .content(setDto.getFeedback())
              .workoutSet(workoutSet)
              .build();
          workoutSet.addFeedback(feedback);
        }
      });
    });

    if (request.getLogFeedback() != null && !request.getLogFeedback().isBlank()) {
      Feedback feedback = Feedback.builder()
          .author(user)
          .content(request.getLogFeedback())
          .workoutLog(workoutLog)
          .build();
      workoutLog.addFeedback(feedback);
    }

    return workoutLogRepository.save(workoutLog).getId();
  }

  public WorkoutLogResponse findWorkoutLogById(Long workoutLogId) {
    // N+1 문제 해결을 위해 Fetch Join 사용을 권장합니다. (Repository에서 구현)
    WorkoutLog workoutLog = workoutLogRepository.findByIdWithDetails(workoutLogId)
        .orElseThrow(() -> new EntityNotFoundException("운동일지를 찾을 수 없습니다. ID: " + workoutLogId));
    return WorkoutLogResponse.from(workoutLog);
  }

  /**
   * [신규] 운동일지 삭제 서비스
   */
  @Transactional
  public void deleteWorkoutLog(Long workoutLogId, Long userId) {
    WorkoutLog workoutLog = workoutLogRepository.findById(workoutLogId)
        .orElseThrow(() -> new EntityNotFoundException("삭제할 운동일지를 찾을 수 없습니다. ID: " + workoutLogId));

    // 본인의 운동일지만 삭제 가능하도록 권한 확인
    if (!workoutLog.getUser().getId().equals(userId)) {
      // 실제 프로젝트에서는 Custom Exception을 사용하는 것이 좋습니다.
      throw new SecurityException("해당 운동일지를 삭제할 권한이 없습니다.");
    }

    // CascadeType.ALL + orphanRemoval=true 옵션에 의해
    // workoutLog만 삭제해도 하위의 WorkoutExercise, WorkoutSet, Feedback이 모두 연쇄적으로 삭제됩니다.
    workoutLogRepository.delete(workoutLog);
  }

  //todo
  //트레이터가 운동일지id or 운동셋id를 통해 피드백 작성 가능
  //서비스 단에서 내가 pt받고 있는 트레이너가 맞는지 확인하는 validation등 검증 코드 필수
}