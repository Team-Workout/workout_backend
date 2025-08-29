package com.workout.pt.service.session;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.PTErrorCode;
import com.workout.member.domain.Member;
import com.workout.pt.domain.contract.PTAppointment;
import com.workout.pt.domain.contract.PTAppointmentStatus;
import com.workout.pt.domain.session.PTSession;
import com.workout.pt.dto.request.PTSessionCreateRequest;
import com.workout.pt.dto.response.PTSessionResponse;
import com.workout.pt.repository.PTAppointmentRepository;
import com.workout.pt.repository.PTSessionRepository;
import com.workout.pt.service.contract.PTContractService;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.service.TrainerService;
import com.workout.workout.domain.log.Feedback;
import com.workout.workout.domain.log.WorkoutExercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutSet;
import com.workout.workout.dto.log.WorkoutLogResponse;
import com.workout.workout.repository.log.FeedbackRepository;
import com.workout.workout.repository.log.WorkoutExerciseRepository;
import com.workout.workout.repository.log.WorkoutSetRepository;
import com.workout.workout.service.WorkoutLogService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PTSessionService {

  private final PTSessionRepository ptSessionRepository;
  private final WorkoutLogService workoutLogService;
  private final PTAppointmentRepository ptAppointmentRepository;
  private final PTContractService ptContractService;
  private final TrainerService trainerService;
  private final WorkoutExerciseRepository workoutExerciseRepository;
  private final WorkoutSetRepository workoutSetRepository;
  private final FeedbackRepository feedbackRepository;

  public PTSessionService(PTSessionRepository ptSessionRepository,
      WorkoutLogService workoutLogService, PTAppointmentRepository ptAppointmentRepository,
      PTContractService ptContractService, TrainerService trainerService,
      WorkoutExerciseRepository workoutExerciseRepository, WorkoutSetRepository workoutSetRepository,
      FeedbackRepository feedbackRepository) {
    this.ptSessionRepository = ptSessionRepository;
    this.ptAppointmentRepository = ptAppointmentRepository;
    this.workoutLogService = workoutLogService;
    this.ptContractService = ptContractService;
    this.trainerService = trainerService;
    this.workoutExerciseRepository = workoutExerciseRepository;
    this.workoutSetRepository = workoutSetRepository;
    this.feedbackRepository = feedbackRepository;
  }

  @Transactional
  public Long createPTSessionAndWorkoutLog(PTSessionCreateRequest request,
      Long userId) {
    Trainer trainer = trainerService.findById(userId);

    PTAppointment appointment = ptAppointmentRepository.findById(request.appointmentId())
        .orElseThrow(
            () -> new RestApiException(PTErrorCode.NOT_FOUND_PT_APPOINTMENT));

    if (appointment.getStatus() != PTAppointmentStatus.SCHEDULED) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
    }

    if (!appointment.getContract().getTrainer().getId().equals(trainer.getId())) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }

    WorkoutLog workoutLog = workoutLogService.createWorkoutLog(request.workoutLog(), userId);

    PTSession ptSession = PTSession.builder()
        .workoutLog(workoutLog)
        .appointment(appointment)
        .build();
    ptSessionRepository.save(ptSession);

    appointment.setStatus(PTAppointmentStatus.COMPLETED);

    ptContractService.deductSession(appointment.getContract().getId());

    return ptSession.getId();
  }

  @Transactional
  public void deletePTSession(Long ptSessionId, Long userId) {
    PTSession ptSession = ptSessionRepository.findById(ptSessionId)
        .orElseThrow(() -> new RestApiException(PTErrorCode.NOT_FOUND_PT_SESSION));

    Member logOwner = ptSession.getWorkoutLog().getMember();
    boolean isOwner = logOwner.getId().equals(userId);
    boolean isTrainer = ptSession.getAppointment().getContract().getTrainer().getId()
        .equals(userId);

    if (!isOwner && !isTrainer) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }

    Long workoutLogIdToDelete = ptSession.getWorkoutLog().getId();

    ptSessionRepository.delete(ptSession);

    workoutLogService.deleteWorkoutLog(workoutLogIdToDelete, logOwner.getId());
  }

  public Page<PTSessionResponse> findMySession(Long userId, Pageable pageable) {
    Page<PTSession> ptSessionPage = ptSessionRepository.findByMemberIdWithDetails(userId, pageable);

    if (ptSessionPage.isEmpty()) {
      return Page.empty(pageable);
    }

    List<WorkoutLog> workoutLogs = ptSessionPage.getContent().stream()
        .map(PTSession::getWorkoutLog)
        .toList();

    List<Long> workoutLogIds = workoutLogs.stream().map(WorkoutLog::getId).toList();

    List<WorkoutExercise> exercises = workoutExerciseRepository.findAllByWorkoutLogIdInOrderByOrderAsc(workoutLogIds);
    List<Long> exerciseIds = exercises.stream().map(WorkoutExercise::getId).toList();

    List<WorkoutSet> sets = exerciseIds.isEmpty() ? Collections.emptyList()
        : workoutSetRepository.findAllByWorkoutExerciseIdInOrderByOrderAsc(exerciseIds);
    List<Long> setIds = sets.stream().map(WorkoutSet::getId).toList();

    List<Feedback> feedbacks = feedbackRepository.findByWorkoutElements(workoutLogIds, exerciseIds, setIds);


    Map<Long, List<WorkoutExercise>> exercisesByLogId = exercises.stream()
        .collect(Collectors.groupingBy(ex -> ex.getWorkoutLog().getId()));

    Map<Long, List<WorkoutSet>> setsByExerciseId = sets.stream()
        .collect(Collectors.groupingBy(set -> set.getWorkoutExercise().getId()));

    return ptSessionPage.map(ptSession -> {
      WorkoutLog log = ptSession.getWorkoutLog();

      List<WorkoutExercise> exercisesForLog = exercisesByLogId.getOrDefault(log.getId(), Collections.emptyList());

      List<WorkoutSet> setsForLog = exercisesForLog.stream()
          .flatMap(ex -> setsByExerciseId.getOrDefault(ex.getId(), Collections.emptyList()).stream())
          .toList();

      WorkoutLogResponse workoutLogResponse = WorkoutLogResponse.from(log, exercisesForLog, setsForLog, feedbacks);

      return new PTSessionResponse(ptSession.getId(), workoutLogResponse);
    });
  }
}
