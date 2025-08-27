package com.workout.pt.service.session;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.PTErrorCode;
import com.workout.member.domain.Member;
import com.workout.pt.domain.contract.PTAppointment;
import com.workout.pt.domain.contract.PTAppointmentStatus;
import com.workout.pt.domain.session.PTSession;
import com.workout.pt.dto.request.PTSessionCreateRequest;
import com.workout.pt.repository.PTAppointmentRepository;
import com.workout.pt.repository.PTSessionRepository;
import com.workout.pt.service.contract.PTContractService;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.service.TrainerService;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.service.WorkoutLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PTSessionService {

  private final PTSessionRepository ptSessionRepository;
  private final WorkoutLogService workoutLogService;
  private final PTAppointmentRepository ptAppointmentRepository;
  private final PTContractService ptContractService;
  private final TrainerService trainerService;

  public PTSessionService(PTSessionRepository ptSessionRepository,
      WorkoutLogService workoutLogService, PTAppointmentRepository ptAppointmentRepository,
      PTContractService ptContractService, TrainerService trainerService) {
    this.ptSessionRepository = ptSessionRepository;
    this.ptAppointmentRepository = ptAppointmentRepository;
    this.workoutLogService = workoutLogService;
    this.ptContractService = ptContractService;
    this.trainerService = trainerService;
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
}
