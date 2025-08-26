package com.workout.workout.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.MemberErrorCode;
import com.workout.global.exception.errorcode.WorkoutErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import com.workout.member.service.MemberService;
import com.workout.pt.service.contract.PTContractService;
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
  private final MemberService memberService;
  private final PTContractService ptContractService;

  public WorkoutLogService(WorkoutLogRepository workoutLogRepository,
      MemberRepository userRepository,
      ExerciseRepository exerciseRepository, FeedbackRepository feedbackRepository,
      WorkoutSetRepository workoutSetRepository,
      WorkoutExerciseRepository workoutExerciseRepository,
      MemberService memberService,
      PTContractService ptContractService) {
    this.workoutLogRepository = workoutLogRepository;
    this.userRepository = userRepository;
    this.exerciseRepository = exerciseRepository;
    this.feedbackRepository = feedbackRepository;
    this.workoutSetRepository = workoutSetRepository;
    this.workoutExerciseRepository = workoutExerciseRepository;
    this.memberService = memberService;
    this.ptContractService = ptContractService;
  }

  @Transactional
  public WorkoutLog createWorkoutLog(WorkoutLogCreateRequest request,
      Long userId) {
    Member member = userRepository.findById(userId)
        .orElseThrow(() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));

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
        throw new RestApiException(WorkoutErrorCode.NOT_FOUND_EXERCISE);
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

    return workoutLog;
  }

  public WorkoutLogResponse findWorkoutLogById(Long workoutLogId, UserPrincipal userPrincipal) {
    Member member = memberService.findById(userPrincipal.getUserId());

    WorkoutLog workoutLog = workoutLogRepository.findById(workoutLogId)
        .orElseThrow(() -> new RestApiException(WorkoutErrorCode.NOT_FOUND_WORKOUT_LOG));

    hasGetAuthority(member, workoutLog);

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
    hasDeleteAuthority(workoutLogId, userId);

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

  void hasGetAuthority(Member member, WorkoutLog workoutLog) {
    if (member.getId().equals(workoutLog.getMember().getId())) {
      return;
    }

    if (!Role.TRAINER.equals(member.getRole())) {
      throw new RestApiException(WorkoutErrorCode.NOT_ALLOWED_ACCESS);
    }

    if (!ptContractService.isMyClient(member.getId(), workoutLog.getMember().getId())) {
      throw new RestApiException(WorkoutErrorCode.NOT_ALLOWED_ACCESS);
    }

    if(!workoutLog.getMember().getIsOpenWorkoutRecord()) {
      throw new RestApiException(WorkoutErrorCode.NOT_ALLOWED_ACCESS);
    }
  }

  void hasDeleteAuthority(Long workoutLogId, Long userId) {
    boolean hasAuthority = workoutLogRepository.existsByIdAndMemberId(workoutLogId, userId);
    if (!hasAuthority) {
      throw new RestApiException(WorkoutErrorCode.NOT_ALLOWED_ACCESS);
    }
  }
}