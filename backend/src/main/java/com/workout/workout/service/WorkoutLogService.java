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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션을 사용
public class WorkoutLogService {

  private final WorkoutLogRepository workoutLogRepository;
  private final UserRepository userRepository;
  private final ExerciseRepository exerciseRepository;
  private final FeedbackRepository feedbackRepository;
  private final WorkoutSetRepository workoutSetRepository;
  private final WorkoutExerciseRepository workoutExerciseRepository;


  public WorkoutLogService(WorkoutLogRepository workoutLogRepository, UserRepository userRepository,
      ExerciseRepository exerciseRepository, FeedbackRepository feedbackRepository,
      WorkoutSetRepository workoutSetRepository, WorkoutExerciseRepository workoutExerciseRepository) {
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

    // 1. 부모 객체 생성 및 '먼저' 저장하여 영속화하고 ID를 확보합니다.
    WorkoutLog workoutLog = WorkoutLog.builder()
        .user(user)
        .workoutDate(request.workoutDate())
        .build();
    workoutLogRepository.save(workoutLog);

    Map<Long, Exercise> exerciseMap = exerciseRepository.findAllByIdIn(
            request.workoutExercises().stream()
                .map(WorkoutLogCreateRequest.WorkoutExerciseDto::exerciseId)
                .distinct().toList())
        .stream().collect(Collectors.toMap(Exercise::getId, e -> e));

    // 2. 자식 객체들을 순회하며 생성하고, 부모와 연관관계를 맺은 후 '명시적으로' 저장합니다.
    request.workoutExercises().forEach(exerciseDto -> {
      Exercise exercise = exerciseMap.get(exerciseDto.exerciseId());
      if (exercise == null) {
        throw new EntityNotFoundException("운동 정보를 찾을 수 없습니다. ID: " + exerciseDto.exerciseId());
      }

      WorkoutExercise workoutExercise = WorkoutExercise.builder()
          .exercise(exercise).order(exerciseDto.order()).build();
      workoutLog.addWorkoutExercise(workoutExercise);
      workoutExerciseRepository.save(workoutExercise); // [명시적 저장]

      exerciseDto.workoutSets().forEach(setDto -> {
        WorkoutSet workoutSet = WorkoutSet.builder()
            .order(setDto.order()).weight(setDto.weight()).reps(setDto.reps()).build();
        workoutExercise.addWorkoutSet(workoutSet);
        workoutSetRepository.save(workoutSet); // [명시적 저장]

        if (setDto.feedback() != null && !setDto.feedback().isBlank()) {
          Feedback feedback = Feedback.builder()
              .author(user).content(setDto.feedback()).workoutSet(workoutSet).build();
          workoutSet.addFeedback(feedback);
          feedbackRepository.save(feedback); // [명시적 저장]
        }
      });
    });

    if (request.logFeedback() != null && !request.logFeedback().isBlank()) {
      Feedback feedback = Feedback.builder()
          .author(user).content(request.logFeedback()).workoutLog(workoutLog).build();
      workoutLog.addFeedback(feedback);
      feedbackRepository.save(feedback); // [명시적 저장]
    }

    return workoutLog.getId();
  }

  public WorkoutLogResponse findWorkoutLogById(Long workoutLogId) {
    WorkoutLog workoutLog = workoutLogRepository.findByIdWithDetails(workoutLogId)
        .orElseThrow(() -> new EntityNotFoundException("운동일지를 찾을 수 없습니다. ID: " + workoutLogId));
    return WorkoutLogResponse.from(workoutLog);
  }

  @Transactional
  public void deleteWorkoutLog(Long workoutLogId, Long userId) {
    boolean hasAuthority = workoutLogRepository.existsByIdAndUserId(workoutLogId, userId);
    if (!hasAuthority) {
      throw new SecurityException("운동일지가 존재하지 않거나 삭제할 권한이 없습니다.");
    }

    // [ID 조회] 삭제할 자식 및 손자 엔티티들의 ID를 미리 조회합니다.
    // 이 과정에서 영속성 컨텍스트는 전혀 사용되지 않습니다.
    List<Long> exerciseIds = workoutExerciseRepository.findIdsByWorkoutLogId(workoutLogId);

    List<Long> setIds = List.of();
    if (!exerciseIds.isEmpty()) {
      setIds = workoutSetRepository.findIdsByWorkoutExerciseIdIn(exerciseIds);
    }

    // 가장 최하위 자손인 Feedback 삭제
    if (!setIds.isEmpty()) {
      feedbackRepository.deleteAllByWorkoutSetIdIn(setIds);
    }
    if (!exerciseIds.isEmpty()) {
      feedbackRepository.deleteAllByWorkoutExerciseIdIn(exerciseIds);
    }
    feedbackRepository.deleteAllByWorkoutLogId(workoutLogId);

    // 그 다음 자식인 WorkoutSet 삭제
    if (!exerciseIds.isEmpty()) {
      workoutSetRepository.deleteAllByWorkoutExerciseIdIn(exerciseIds);
    }

    // 그 다음 자식인 WorkoutExercise 삭제
    if (!exerciseIds.isEmpty()) { // workoutLog에 exercise가 없는 경우도 고려
      workoutExerciseRepository.deleteAllByWorkoutLogId(workoutLogId);
    }

    // 모든 자식들이 삭제된 후, 부모인 WorkoutLog를 삭제
    workoutLogRepository.deleteById(workoutLogId);
  }

  //todo
  //트레이터가 운동일지id or 운동셋id를 통해 피드백 작성 가능
  //서비스 단에서 내가 pt받고 있는 트레이너가 맞는지 확인하는 validation등 검증 코드 필수
}