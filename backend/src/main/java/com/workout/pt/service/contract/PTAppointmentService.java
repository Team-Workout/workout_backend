package com.workout.pt.service.contract;


import com.workout.auth.domain.UserPrincipal;
import com.workout.pt.domain.contract.PTAppointment;
import com.workout.pt.domain.contract.PTAppointmentStatus;
import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import com.workout.pt.dto.request.AppointmentRequest;
import com.workout.pt.dto.request.AppointmentUpdateRequest;
import com.workout.pt.dto.response.AppointmentResponse;
import com.workout.pt.repository.PTAppointmentRepository;
import com.workout.pt.repository.PTContractRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PTAppointmentService {

  private final PTAppointmentRepository ptAppointmentRepository;
  private final PTContractRepository ptContractRepository;
  private final PTContractService ptContractService;

  public PTAppointmentService(PTAppointmentRepository ptAppointmentRepository,
      PTContractRepository ptContractRepository, PTContractService ptContractService) {
    this.ptAppointmentRepository = ptAppointmentRepository;
    this.ptContractRepository = ptContractRepository;
    this.ptContractService = ptContractService;
  }

  public List<AppointmentResponse> findMyScheduledAppointmentsByPeriod(
      UserPrincipal user, LocalDate startDate, LocalDate endDate) {

    // 1. 기간 유효성 검증 (최대 7일)
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
    }
    if (Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays() >= 7) {
      throw new IllegalArgumentException("조회 기간은 최대 7일까지 가능합니다.");
    }

    // LocalDate를 LocalDateTime으로 변환 (하루의 시작과 끝)
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

    List<PTAppointment> appointments;

    // 2. 역할 확인
    boolean isTrainer = user.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_TRAINER"));

    // 3. 역할에 맞는 Repository 메소드 호출
    if (isTrainer) {
      appointments = ptAppointmentRepository.findAllByContract_Trainer_IdAndStatusAndStartTimeBetween(
          user.getUserId(), PTAppointmentStatus.SCHEDULED, startDateTime, endDateTime);
    } else {
      appointments = ptAppointmentRepository.findAllByContract_Member_IdAndStatusAndStartTimeBetween(
          user.getUserId(), PTAppointmentStatus.SCHEDULED, startDateTime, endDateTime);
    }

    // 4. Entity List -> DTO List 변환하여 반환
    return appointments.stream()
        .map(AppointmentResponse::from)
        .collect(Collectors.toList());
  }

  @Transactional
  public Long create(UserPrincipal user, AppointmentRequest request) {
    // 엔티티 조회
    PTContract contract = ptContractRepository.findById(request.contractId())
        .orElseThrow(() -> new EntityNotFoundException("계약 정보를 찾을 수 없습니다."));

    // 권한 확인 (요청자가 해당 계약의 트레이너인지 확인)
    if (!contract.getTrainer().getId().equals(user.getUserId())) {
      throw new AccessDeniedException("스케줄을 생성할 권한이 있는 트레이너가 아닙니다.");
    }

    // 계약 상태 및 남은 세션 확인
    if (contract.getStatus() != PTContractStatus.ACTIVE) {
      throw new IllegalStateException("현재 활성 상태인 계약만 예약을 생성할 수 있습니다.");
    }
    if (contract.getRemainingSessions() <= 0) {
      throw new IllegalStateException("남은 PT 세션이 없습니다.");
    }

    // 트레이너의 스케줄 중복 확인
    checkTrainerScheduleOverlap(contract.getTrainer().getId(), request.startTime(),
        request.endTime());

    PTAppointment appointment = PTAppointment.builder()
        .contract(contract)
        .startTime(request.startTime())
        .endTime(request.endTime())
        .status(PTAppointmentStatus.SCHEDULED) // 초기 상태는 '예약됨'
        .build();

    return ptAppointmentRepository.save(appointment).getId();
  }

  private void checkTrainerScheduleOverlap(Long trainerId, LocalDateTime startTime,
      LocalDateTime endTime) {
    // 이미 해당 트레이너에게 잡힌 예약 중, 요청된 시간과 겹치는 예약이 있는지 확인
    // 겹치는 조건: (new.start < old.end) AND (new.end > old.start)
    if (ptAppointmentRepository.existsOverlappingAppointment(trainerId, startTime, endTime)) {
      throw new IllegalStateException("해당 시간에 이미 다른 예약이 존재합니다.");
    }
  }

  @Transactional
  public void updateStatus(UserPrincipal user, Long appointmentId, PTAppointmentStatus status) {
    PTAppointment appointment = ptAppointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다."));

    if (!appointment.getContract().getTrainer().getId().equals(user.getUserId())) {
      throw new IllegalStateException("트레이너만 완료로 변경할 수 있습니다.");
    }

    appointment.setStatus(status);
    ptAppointmentRepository.save(appointment);

    // 수업이 '완료(COMPLETED)'로 변경되면, 계약의 남은 세션을 차감
    if (status == PTAppointmentStatus.COMPLETED) {
      ptContractService.deductSession(appointment.getContract().getId());
    }
  }

  public Long propose(UserPrincipal user, AppointmentRequest request) {
    PTContract contract = ptContractRepository.findById(request.contractId())
        .orElseThrow(() -> new EntityNotFoundException("계약 정보를 찾을 수 없습니다."));

    // 권한 확인 (요청자가 계약의 회원인지)
    if (!contract.getMember().getId().equals(user.getUserId())) {
      throw new AccessDeniedException("자신의 계약에 대해서만 PT 시간을 제안할 수 있습니다.");
    }

    // 계약 상태 및 남은 세션 확인
    if (contract.getStatus() != PTContractStatus.ACTIVE) {
      throw new IllegalStateException("현재 활성 상태인 계약만 예약을 생성할 수 있습니다.");
    }
    if (contract.getRemainingSessions() <= 0) {
      throw new IllegalStateException("남은 PT 세션이 없습니다.");
    }

    // 트레이너의 스케줄 중복 확인
    checkTrainerScheduleOverlap(contract.getTrainer().getId(), request.startTime(),
        request.endTime());

    PTAppointment appointment = PTAppointment.builder()
        .contract(contract)
        .startTime(request.startTime())
        .endTime(request.endTime())
        .status(PTAppointmentStatus.MEMBER_REQUESTED) // '회원 요청' 상태로 생성
        .build();

    return ptAppointmentRepository.save(appointment).getId();
  }

  @Transactional
  public void confirm(UserPrincipal trainer, Long appointmentId) {
    PTAppointment appointment = ptAppointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다."));

    // 권한 확인 (트레이너인지)
    if (!appointment.getContract().getTrainer().getId().equals(trainer.getUserId())) {
      throw new AccessDeniedException("예약을 확정할 권한이 없습니다.");
    }

    // 상태 확인
    if (appointment.getStatus() != PTAppointmentStatus.MEMBER_REQUESTED) {
      throw new IllegalStateException("회원이 요청한 상태의 예약만 확정할 수 있습니다.");
    }

    appointment.setStatus(PTAppointmentStatus.SCHEDULED);
    ptAppointmentRepository.save(appointment);
  }

  public void requestChange(UserPrincipal user, Long appointmentId,
      AppointmentUpdateRequest request) {
    PTAppointment appointment = findAppointmentById(appointmentId);

    // 권한 확인: 본인(회원)의 예약인지 확인
    if (!appointment.getContract().getMember().getId().equals(user.getUserId())) {
      throw new AccessDeniedException("본인의 예약만 변경을 요청할 수 있습니다.");
    }
    // 상태 확인: 확정된 스케줄만 변경 요청 가능
    if (appointment.getStatus() != PTAppointmentStatus.SCHEDULED) {
      throw new IllegalStateException("확정된 예약만 변경을 요청할 수 있습니다.");
    }

    // 변경을 제안한 시간이 다른 스케줄과 겹치는지 확인
    checkTrainerScheduleOverlap(appointment.getContract().getTrainer().getId(),
        request.newStartTime(), request.newEndTime());

    appointment.setProposedStartTime(request.newStartTime());
    appointment.setProposedEndTime(request.newEndTime());
    appointment.setStatus(PTAppointmentStatus.CHANGE_REQUESTED);
  }

  /**
   * 트레이너가 PT 스케줄 변경을 요청합니다.
   */
  public void requestChangeByTrainer(UserPrincipal trainer, Long appointmentId,
      AppointmentUpdateRequest request) {
    PTAppointment appointment = findAppointmentById(appointmentId);

    // 권한 확인: 담당 트레이너인지 확인
    if (!appointment.getContract().getTrainer().getId().equals(trainer.getUserId())) {
      throw new AccessDeniedException("담당 회원의 예약만 변경을 요청할 수 있습니다.");
    }
    // 상태 확인: 확정된 스케줄만 변경 요청 가능
    if (appointment.getStatus() != PTAppointmentStatus.SCHEDULED) {
      throw new IllegalStateException("확정된 예약만 변경을 요청할 수 있습니다.");
    }

    // 변경을 제안한 시간이 다른 스케줄과 겹치는지 확인
    checkTrainerScheduleOverlap(appointment.getContract().getTrainer().getId(),
        request.newStartTime(), request.newEndTime());

    appointment.setProposedStartTime(request.newStartTime());
    appointment.setProposedEndTime(request.newEndTime());
    appointment.setStatus(PTAppointmentStatus.TRAINER_CHANGE_REQUESTED);
  }

  /**
   * 트레이너가 회원의 변경 요청을 수락합니다.
   */
  public void approveChange(UserPrincipal user, Long appointmentId) {
    PTAppointment appointment = findAppointmentById(appointmentId);

    // 권한 확인: 담당 트레이너인지 확인
    if (!appointment.getContract().getTrainer().getId().equals(user.getUserId())) {
      throw new AccessDeniedException("변경 요청을 수락할 권한이 없습니다.");
    }
    // 상태 확인: 회원이 변경 요청한 상태인지 확인
    if (appointment.getStatus() != PTAppointmentStatus.CHANGE_REQUESTED) {
      throw new IllegalStateException("회원이 변경 요청한 예약만 수락할 수 있습니다.");
    }

    // 제안된 시간으로 실제 예약 시간을 업데이트
    appointment.setStartTime(appointment.getProposedStartTime());
    appointment.setEndTime(appointment.getProposedEndTime());

    // 제안 시간 필드는 초기화
    appointment.setProposedStartTime(null);
    appointment.setProposedEndTime(null);
    appointment.setStatus(PTAppointmentStatus.SCHEDULED); // 다시 '확정' 상태로 변경
  }

  /**
   * 회원이 트레이너의 변경 요청을 수락합니다.
   */
  public void approveChangeByMember(UserPrincipal member, Long appointmentId) {
    PTAppointment appointment = findAppointmentById(appointmentId);

    // 권한 확인: 예약의 당사자(회원)인지 확인
    if (!appointment.getContract().getMember().getId().equals(member.getUserId())) {
      throw new AccessDeniedException("변경 요청을 수락할 권한이 없습니다.");
    }
    // 상태 확인: 트레이너가 변경 요청한 상태인지 확인
    if (appointment.getStatus() != PTAppointmentStatus.TRAINER_CHANGE_REQUESTED) {
      throw new IllegalStateException("트레이너가 변경 요청한 예약만 수락할 수 있습니다.");
    }

    // 제안된 시간으로 실제 예약 시간을 업데이트
    appointment.setStartTime(appointment.getProposedStartTime());
    appointment.setEndTime(appointment.getProposedEndTime());

    // 제안 시간 필드는 초기화
    appointment.setProposedStartTime(null);
    appointment.setProposedEndTime(null);
    appointment.setStatus(PTAppointmentStatus.SCHEDULED); // 다시 '확정' 상태로 변경
  }

  /**
   * PT 스케줄 변경 요청을 거절합니다.
   */
  public void rejectChange(UserPrincipal user, Long appointmentId) {
    PTAppointment appointment = findAppointmentById(appointmentId);

    boolean isMember = user.getUserId().equals(appointment.getContract().getMember().getId());
    boolean isTrainer = user.getUserId().equals(appointment.getContract().getTrainer().getId());

    // 거절 권한 확인: 트레이너가 회원의 요청을 거절하거나, 회원이 트레이너의 요청을 거절하는 경우
    boolean canReject =
        (isTrainer && appointment.getStatus() == PTAppointmentStatus.CHANGE_REQUESTED) ||
            (isMember && appointment.getStatus() == PTAppointmentStatus.TRAINER_CHANGE_REQUESTED);

    if (!canReject) {
      throw new AccessDeniedException("변경 요청을 거절할 수 없는 상태이거나 권한이 없습니다.");
    }

    // 제안 시간 필드만 초기화하고 원래 시간으로 되돌림
    appointment.setProposedStartTime(null);
    appointment.setProposedEndTime(null);
    appointment.setStatus(PTAppointmentStatus.SCHEDULED); // 다시 '확정' 상태로 변경
  }

  private PTAppointment findAppointmentById(Long appointmentId) {
    return ptAppointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다. ID: " + appointmentId));
  }
}