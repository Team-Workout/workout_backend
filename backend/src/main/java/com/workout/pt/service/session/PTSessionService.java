package com.workout.pt.service.session;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.pt.domain.contract.PTAppointment;
import com.workout.pt.domain.contract.PTAppointmentStatus;
import com.workout.pt.domain.session.PTSession;
import com.workout.pt.dto.request.PTSessionCreateRequest;
import com.workout.pt.repository.PTAppointmentRepository;
import com.workout.pt.repository.PTSessionRepository;
import com.workout.pt.service.contract.PTContractService;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.service.WorkoutLogService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PTSessionService {

  private final PTSessionRepository ptSessionRepository;
  private final WorkoutLogService workoutLogService;
  private final PTAppointmentRepository ptAppointmentRepository;
  private final PTContractService ptContractService;

  public PTSessionService(PTSessionRepository ptSessionRepository,
      WorkoutLogService workoutLogService, PTAppointmentRepository ptAppointmentRepository,
      PTContractService ptContractService) {
    this.ptSessionRepository = ptSessionRepository;
    this.ptAppointmentRepository = ptAppointmentRepository;
    this.workoutLogService = workoutLogService;
    this.ptContractService = ptContractService;
  }

  @Transactional
  public Long createPTSessionAndWorkoutLog(PTSessionCreateRequest request,
      UserPrincipal userPrincipal) {
    boolean isTrainer = userPrincipal.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_TRAINER"));
    if (!isTrainer) {
      throw new AccessDeniedException("트레이너만 PT 세션을 생성할 수 있습니다.");
    }

    PTAppointment appointment = ptAppointmentRepository.findById(request.appointmentId())
        .orElseThrow(
            () -> new EntityNotFoundException("PT 예약을 찾을 수 없습니다. ID: " + request.appointmentId()));

    if (appointment.getStatus() != PTAppointmentStatus.SCHEDULED) {
      throw new IllegalStateException("예약(SCHEDULED) 상태인 수업에 대해서만 세션을 생성할 수 있습니다.");
    }

    if (!appointment.getContract().getTrainer().getId().equals(userPrincipal.getUserId())) {
      throw new AccessDeniedException("담당하는 PT 수업에 대해서만 세션을 생성할 수 있습니다.");
    }

    UserPrincipal memberPrincipal = new UserPrincipal(appointment.getContract().getMember());
    WorkoutLog workoutLog = workoutLogService.createWorkoutLog(request.workoutLog(),
        memberPrincipal);

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
  public void deletePTSession(Long ptSessionId, UserPrincipal user) {
    PTSession ptSession = ptSessionRepository.findById(ptSessionId)
        .orElseThrow(() -> new EntityNotFoundException("PT 세션을 찾을 수 없습니다. ID: " + ptSessionId));

    Member logOwner = ptSession.getWorkoutLog().getMember();
    boolean isOwner = logOwner.getId().equals(user.getUserId());
    boolean isTrainer = ptSession.getAppointment().getContract().getTrainer().getId()
        .equals(user.getUserId());

    if (!isOwner && !isTrainer) {
      throw new AccessDeniedException("세션을 삭제할 권한이 없습니다.");
    }

    Long workoutLogIdToDelete = ptSession.getWorkoutLog().getId();

    ptSessionRepository.delete(ptSession);

    workoutLogService.deleteWorkoutLog(workoutLogIdToDelete, logOwner.getId());
  }
}
