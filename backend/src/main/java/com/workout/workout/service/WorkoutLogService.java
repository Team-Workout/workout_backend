package com.workout.workout.service;

import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.log.Feedback;
import com.workout.workout.domain.log.WorkoutExercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutSet;
import com.workout.workout.dto.log.WorkoutLogCreateRequest;
import com.workout.workout.dto.log.WorkoutLogResponse;
import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.FeedbackRepository;
import com.workout.workout.repository.WorkoutExerciseRepository;
import com.workout.workout.repository.WorkoutLogRepository;
import com.workout.workout.repository.WorkoutSetRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션을 사용
public class WorkoutLogService {

  private final WorkoutLogRepository workoutLogRepository;
  private final UserRepository userRepository;
  private final ExerciseRepository exerciseRepository;
  private final FeedbackRepository feedbackRepository;
  private final WorkoutLogRepository workoutSetRepository;
  private final WorkoutExerciseRepository workoutExerciseRepository;


  public WorkoutLogService(WorkoutLogRepository workoutLogRepository, UserRepository userRepository,
      ExerciseRepository exerciseRepository, FeedbackRepository feedbackRepository,
      WorkoutLogRepository workoutSetRepository, WorkoutExerciseRepository workoutExerciseRepository) {
    this.workoutLogRepository = workoutLogRepository;
    this.userRepository = userRepository;
    this.exerciseRepository = exerciseRepository;
    this.feedbackRepository = feedbackRepository;
    this.workoutSetRepository = workoutSetRepository;
    this.workoutExerciseRepository = workoutExerciseRepository;
  }

  @Transactional
  public Long createWorkoutLog(WorkoutLogCreateRequest request, Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

    WorkoutLog workoutLog = WorkoutLog.builder()
        .user(user)
        .workoutDate(request.workoutDate())
        .build();

    List<Long> exerciseIds = request.workoutExercises().stream()
        .map(WorkoutLogCreateRequest.WorkoutExerciseDto::exerciseId)
        .distinct()
        .toList();

    Map<Long, Exercise> exerciseMap = exerciseRepository.findAllByIdIn(exerciseIds).stream()
        .collect(Collectors.toMap(Exercise::getId, e -> e));

    request.workoutExercises().forEach(exerciseDto -> {
      Exercise exercise = exerciseMap.get(exerciseDto.exerciseId());
      if (exercise == null) {
        throw new EntityNotFoundException("운동 정보를 찾을 수 없습니다. ID: " + exerciseDto.exerciseId());
      }

      WorkoutExercise workoutExercise = WorkoutExercise.builder()
          .exercise(exercise)
          .order(exerciseDto.order())
          .build();
      workoutLog.addWorkoutExercise(workoutExercise);

      exerciseDto.workoutSets().forEach(setDto -> {
        WorkoutSet workoutSet = WorkoutSet.builder()
            .order(setDto.order())
            .weight(setDto.weight())
            .reps(setDto.reps())
            .build();
        workoutExercise.addWorkoutSet(workoutSet);

        if (setDto.feedback() != null && !setDto.feedback().isBlank()) {
          Feedback feedback = Feedback.builder()
              .author(user) // 피드백 작성자는 현재 사용자
              .content(setDto.feedback())
              .workoutSet(workoutSet)
              .build();
          workoutSet.addFeedback(feedback);
        }
      });
    });

    if (request.logFeedback() != null && !request.logFeedback().isBlank()) {
      Feedback feedback = Feedback.builder()
          .author(user)
          .content(request.logFeedback())
          .workoutLog(workoutLog)
          .build();
      workoutLog.addFeedback(feedback);
    }

    return workoutLogRepository.save(workoutLog).getId();
  }

  public WorkoutLogResponse findWorkoutLogById(Long workoutLogId) {
    WorkoutLog workoutLog = workoutLogRepository.findByIdWithDetails(workoutLogId)
        .orElseThrow(() -> new EntityNotFoundException("운동일지를 찾을 수 없습니다. ID: " + workoutLogId));
    return WorkoutLogResponse.from(workoutLog);
  }

  /**
   * [신규] 운동일지 삭제 서비스
   */
  @Transactional
  public void deleteWorkoutLog(Long workoutLogId, Long userId) {
    // 1. 삭제할 WorkoutLog 조회 및 소유권 확인
    WorkoutLog workoutLog = workoutLogRepository.findById(workoutLogId)
        .orElseThrow(() -> new EntityNotFoundException("운동일지를 찾을 수 없습니다."));

    if (!workoutLog.getUser().getId().equals(userId)) {
      throw new SecurityException("운동일지를 삭제할 권한이 없습니다.");
    }

    // 2. 삭제할 대상들의 ID 목록을 미리 추출
    List<WorkoutExercise> exercises = workoutLog.getWorkoutExercises();
    List<Long> exerciseIds = exercises.stream()
        .map(WorkoutExercise::getId)
        .collect(Collectors.toList());

    List<WorkoutSet> sets = exercises.stream()
        .flatMap(exercise -> exercise.getWorkoutSets().stream())
        .collect(Collectors.toList());
    List<Long> setIds = sets.stream()
        .map(WorkoutSet::getId)
        .collect(Collectors.toList());

    // 3. [Bottom-up Deletion] 최하위 자식부터 순차적으로 삭제
    // 3-1. WorkoutSet에 달린 Feedback들 삭제
    if (!setIds.isEmpty()) {
      feedbackRepository.deleteAllByWorkoutSetIdIn(setIds);
    }
    // 3-2. WorkoutExercise에 달린 Feedback들 삭제
    if (!exerciseIds.isEmpty()) {
      feedbackRepository.deleteAllByWorkoutExerciseIdIn(exerciseIds);
    }
    // 3-3. WorkoutLog에 직접 달린 Feedback들 삭제
    feedbackRepository.deleteAllByWorkoutLogId(workoutLogId);

    // 3-4. WorkoutSet들 삭제
    if (!exerciseIds.isEmpty()) {
      workoutSetRepository.deleteAllByWorkoutExerciseIdIn(exerciseIds);
    }

    // 3-5. WorkoutExercise들 삭제
    workoutExerciseRepository.deleteAll(exercises);

    // 4. 모든 자식들이 삭제된 후, 최종적으로 WorkoutLog 삭제
    workoutLogRepository.delete(workoutLog);
  }

  //todo
  //트레이터가 운동일지id or 운동셋id를 통해 피드백 작성 가능
  //서비스 단에서 내가 pt받고 있는 트레이너가 맞는지 확인하는 validation등 검증 코드 필수
}