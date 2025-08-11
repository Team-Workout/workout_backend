package com.workout.workout.service;

import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutSet;
import com.workout.workout.dto.WorkoutLogCreateRequest;
import com.workout.workout.dto.WorkoutLogResponse;
import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.WorkoutLogRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다.
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션을 사용
public class WorkoutLogService {

  private final WorkoutLogRepository workoutLogRepository;
  private final UserRepository userRepository;
  private final ExerciseRepository exerciseRepository;

  /**
   * 운동일지를 저장하는 기능
   */
  @Transactional // 쓰기 작업이므로 별도 트랜잭션 어노테이션을 적용
  public Long createWorkoutLog(WorkoutLogCreateRequest request, Long userId) {
    // 1. 사용자 정보를 조회합니다.
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

    // 2. 운동일지(WorkoutLog) 엔티티를 생성합니다.
    WorkoutLog workoutLog = WorkoutLog.builder()
        .user(user)
        .workoutDate(request.getWorkoutDate())
        .userMemo(request.getUserMemo())
        .build();

    // 3. 요청에 포함된 각 운동 세트(WorkoutSet)를 엔티티로 변환하고 운동일지에 추가합니다.
    AtomicInteger setCounter = new AtomicInteger(1); // 세트 번호를 매기기 위함
    request.getWorkoutSets().forEach(setDto -> {
      Exercise exercise = exerciseRepository.findById(setDto.getExerciseId())
          .orElseThrow(() -> new EntityNotFoundException("운동 정보를 찾을 수 없습니다. ID: " + setDto.getExerciseId()));

      WorkoutSet workoutSet = WorkoutSet.builder()
          .exercise(exercise)
          .weight(setDto.getWeight())
          .reps(setDto.getReps())
          .setNumber(setCounter.getAndIncrement())
          .build();

      workoutLog.addWorkoutSet(workoutSet); // 연관관계 편의 메소드 사용
    });

    // 4. 운동일지를 저장합니다. Cascade 설정 덕분에 WorkoutSet도 함께 저장됩니다.
    WorkoutLog savedLog = workoutLogRepository.save(workoutLog);
    return savedLog.getId();
  }

  /**
   * 운동일지를 ID로 조회하는 기능
   */
  public WorkoutLogResponse findWorkoutLogById(Long workoutLogId) {
    WorkoutLog workoutLog = workoutLogRepository.findByIdWithSets(workoutLogId)
        .orElseThrow(() -> new EntityNotFoundException("운동일지를 찾을 수 없습니다. ID: " + workoutLogId));

    // 엔티티를 DTO로 변환하여 반환
    return new WorkoutLogResponse(workoutLog);
  }
}