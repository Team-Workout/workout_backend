package com.workout.workout.service;

import com.workout.member.domain.Member;
import com.workout.member.repository.MemberRepository;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.log.Feedback;
import com.workout.workout.domain.log.WorkoutExercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutSet;
import com.workout.workout.dto.log.WorkoutLogCreateRequest;
import com.workout.workout.dto.log.WorkoutLogResponse;
import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.log.FeedbackRepository;
import com.workout.workout.repository.log.WorkoutExerciseRepository;
import com.workout.workout.repository.log.WorkoutLogRepository;
import com.workout.workout.repository.log.WorkoutSetRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
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
  private final MemberRepository userRepository;
  private final ExerciseRepository exerciseRepository;
  private final FeedbackRepository feedbackRepository;
  private final WorkoutSetRepository workoutSetRepository;
  private final WorkoutExerciseRepository workoutExerciseRepository;


  public WorkoutLogService(WorkoutLogRepository workoutLogRepository,
      MemberRepository userRepository,
      ExerciseRepository exerciseRepository, FeedbackRepository feedbackRepository,
      WorkoutSetRepository workoutSetRepository,
      WorkoutExerciseRepository workoutExerciseRepository) {
    this.workoutLogRepository = workoutLogRepository;
    this.userRepository = userRepository;
    this.exerciseRepository = exerciseRepository;
    this.feedbackRepository = feedbackRepository;
    this.workoutSetRepository = workoutSetRepository;
    this.workoutExerciseRepository = workoutExerciseRepository;
  }

  @Transactional
  public Long createWorkoutLog(WorkoutLogCreateRequest request, Long userId) {
    Member member = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

    WorkoutLog workoutLog = request.toEntity(member);
    workoutLogRepository.save(workoutLog);

    Map<Long, Exercise> exerciseMap = exerciseRepository.findAllByIdIn(
        request.workoutExercises().stream()
            .map(WorkoutLogCreateRequest.WorkoutExerciseDto::exerciseId)
            .distinct().toList()
    ).stream().collect(Collectors.toMap(Exercise::getId, e -> e));

    List<WorkoutExercise> exercisesToSave = new ArrayList<>();
    List<WorkoutSet> setsToSave = new ArrayList<>();
    List<Feedback> feedbacksToSave = new ArrayList<>();

    request.workoutExercises().forEach(exerciseDto -> {
      Exercise exercise = exerciseMap.get(exerciseDto.exerciseId());
      if (exercise == null) {
        throw new EntityNotFoundException("운동 정보를 찾을 수 없습니다. ID: " + exerciseDto.exerciseId());
      }
      WorkoutExercise workoutExercise = exerciseDto.toEntity(workoutLog, exercise);
      exercisesToSave.add(workoutExercise);

      exerciseDto.workoutSets().forEach(setDto -> {
        WorkoutSet workoutSet = setDto.toEntity(workoutExercise);
        setsToSave.add(workoutSet);

        if (isFeedbackPresent(setDto.feedback())) {
          feedbacksToSave.add(Feedback.builder()
              .author(member).content(setDto.feedback()).workoutSet(workoutSet).build());
        }
      });
    });

    if (isFeedbackPresent(request.logFeedback())) {
      feedbacksToSave.add(Feedback.builder()
          .author(member).content(request.logFeedback()).workoutLog(workoutLog).build());
    }

    workoutExerciseRepository.saveAll(exercisesToSave);
    workoutSetRepository.saveAll(setsToSave);
    feedbackRepository.saveAll(feedbacksToSave);

    return workoutLog.getId();
  }

  public WorkoutLogResponse findWorkoutLogById(Long workoutLogId) {
    // 최상위 엔티티 조회
    WorkoutLog workoutLog = workoutLogRepository.findById(workoutLogId)
        .orElseThrow(() -> new EntityNotFoundException("운동일지를 찾을 수 없습니다. ID: " + workoutLogId));

    // 자식 엔티티 목록 조회 (WorkoutExercise)
    List<WorkoutExercise> exercises = workoutExerciseRepository.findAllByWorkoutLogIdOrderByOrderAsc(
        workoutLogId);
    List<Long> exerciseIds = exercises.stream().map(WorkoutExercise::getId).toList();

    // 손자 엔티티 목록 조회 (WorkoutSet)
    List<WorkoutSet> sets = exerciseIds.isEmpty() ? Collections.emptyList()
        : workoutSetRepository.findAllByWorkoutExerciseIdInOrderByOrderAsc(exerciseIds);
    List<Long> setIds = sets.stream().map(WorkoutSet::getId).toList();

    // 모든 피드백 한 번에 조회
    List<Feedback> feedbacks = feedbackRepository.findByWorkoutElements(workoutLogId, exerciseIds,
        setIds);

    // 조회된 엔티티들을 DTO로 조립
    return WorkoutLogResponse.from(workoutLog, exercises, sets, feedbacks);
  }

  @Transactional
  public void deleteWorkoutLog(Long workoutLogId, Long userId) {
    boolean hasAuthority = workoutLogRepository.existsByIdAndMemberId(workoutLogId, userId);
    if (!hasAuthority) {
      throw new SecurityException("운동일지가 존재하지 않거나 삭제할 권한이 없습니다.");
    }

    // 삭제할 대상 ID 목록 조회 (자식 -> 손자 순)
    List<Long> exerciseIds = workoutExerciseRepository.findIdsByWorkoutLogId(workoutLogId);

    List<Long> setIds = exerciseIds.isEmpty() ? Collections.emptyList()
        : workoutSetRepository.findIdsByWorkoutExerciseIdIn(exerciseIds);

    // 삭제는 반드시 하위 엔티티부터 순서대로 진행 (손자 -> 자식 -> 부모 순)
    // 외래 키 제약 조건(Foreign Key Constraint) 위반을 방지합니다.

    // 피드백 삭제 (가장 하위)
    if (!setIds.isEmpty()) {
      feedbackRepository.deleteAllByWorkoutSetIdIn(setIds);
    }
    if (!exerciseIds.isEmpty()) {
      feedbackRepository.deleteAllByWorkoutExerciseIdIn(exerciseIds);
    }
    feedbackRepository.deleteAllByWorkoutLogId(workoutLogId);

    // 손자(WorkoutSet) 삭제
    if (!exerciseIds.isEmpty()) {
      workoutSetRepository.deleteAllByWorkoutExerciseIdIn(exerciseIds);
    }

    // 자식(WorkoutExercise) 삭제
    if (!exerciseIds.isEmpty()) {
      workoutExerciseRepository.deleteAllByWorkoutLogId(workoutLogId);
    }

    // 최상위 부모(WorkoutLog) 삭제
    workoutLogRepository.deleteById(workoutLogId);
  }

  private boolean isFeedbackPresent(String feedback) {
    return feedback != null && !feedback.isBlank();
  }

  //todo
  //트레이터가 운동일지id or 운동셋id를 통해 피드백 작성 가능
  //서비스 단에서 내가 pt받고 있는 트레이너가 맞는지 확인하는 validation등 검증 코드 필수
}