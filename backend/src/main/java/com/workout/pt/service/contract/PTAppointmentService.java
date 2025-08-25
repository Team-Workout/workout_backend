package com.workout.pt.service.contract;


import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.PTErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.service.MemberService;
import com.workout.pt.domain.contract.PTAppointment;
import com.workout.pt.domain.contract.PTAppointmentStatus;
import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import com.workout.pt.dto.request.AppointmentRequest;
import com.workout.pt.dto.request.AppointmentUpdateRequest;
import com.workout.pt.dto.response.AppointmentResponse;
import com.workout.pt.repository.PTAppointmentRepository;
import com.workout.pt.repository.PTContractRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PTAppointmentService {

  private final PTAppointmentRepository ptAppointmentRepository;
  private final PTContractRepository ptContractRepository;
  private final PTContractService ptContractService;
  private final MemberService memberService;

  public PTAppointmentService(PTAppointmentRepository ptAppointmentRepository,
      PTContractRepository ptContractRepository, PTContractService ptContractService,
      MemberService memberService) {
    this.ptAppointmentRepository = ptAppointmentRepository;
    this.ptContractRepository = ptContractRepository;
    this.ptContractService = ptContractService;
    this.memberService = memberService;
  }

  public List<AppointmentResponse> findMyScheduledAppointmentsByPeriod(
      Long userId, LocalDate startDate, LocalDate endDate) {

    // 1. 기간 유효성 검증 (최대 7일)
    if (startDate.isAfter(endDate)) {
      throw new RestApiException(PTErrorCode.INVALID_PARAMETER);
    }
    if (Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays() >= 7) {
      throw new RestApiException(PTErrorCode.INVALID_PARAMETER);
    }

    // LocalDate를 LocalDateTime으로 변환 (하루의 시작과 끝)
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

    List<PTAppointment> appointments;

    Member member = memberService.findById(userId);

    if (member.getRole().equals(Role.TRAINER)) {
      appointments = ptAppointmentRepository.findAllByContract_Trainer_IdAndStatusAndStartTimeBetween(
          member.getId(), PTAppointmentStatus.SCHEDULED, startDateTime, endDateTime);
    } else {
      appointments = ptAppointmentRepository.findAllByContract_Member_IdAndStatusAndStartTimeBetween(
          member.getId(), PTAppointmentStatus.SCHEDULED, startDateTime, endDateTime);
    }

    return appointments.stream()
        .map(AppointmentResponse::from)
        .collect(Collectors.toList());
  }

  @Transactional
  public Long create(Long userId, AppointmentRequest request) {
    PTContract contract = ptContractRepository.findById(request.contractId())
        .orElseThrow(() -> new RestApiException(PTErrorCode.NOT_FOUND_PT_APPOINTMENT));

    if (!contract.getTrainer().getId().equals(userId)) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }

    if (contract.getStatus() != PTContractStatus.ACTIVE) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
    }
    if (contract.getRemainingSessions() <= 0) {
      throw new RestApiException(PTErrorCode.NO_REMAIN_SESSION);
    }

    checkTrainerScheduleOverlap(contract.getTrainer().getId(), request.startTime(),
        request.endTime());

    PTAppointment appointment = PTAppointment.builder()
        .contract(contract)
        .startTime(request.startTime())
        .endTime(request.endTime())
        .status(PTAppointmentStatus.SCHEDULED)
        .build();

    return ptAppointmentRepository.save(appointment).getId();
  }

  private void checkTrainerScheduleOverlap(Long trainerId, LocalDateTime startTime,
      LocalDateTime endTime) {
    // 이미 해당 트레이너에게 잡힌 예약 중, 요청된 시간과 겹치는 예약이 있는지 확인
    // 겹치는 조건: (new.start < old.end) AND (new.end > old.start)
    if (ptAppointmentRepository.existsOverlappingAppointment(trainerId, startTime, endTime)) {
      throw new RestApiException(PTErrorCode.ALREADY_PRESENT_APPOINTMENT);
    }
  }

  @Transactional
  public void updateStatus(Long userId, Long appointmentId, PTAppointmentStatus status) {
    PTAppointment appointment = ptAppointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new RestApiException(PTErrorCode.NOT_FOUND_PT_APPOINTMENT));

    if (!appointment.getContract().getTrainer().getId().equals(userId)) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }

    appointment.setStatus(status);
    ptAppointmentRepository.save(appointment);

    // 수업이 '완료(COMPLETED)'로 변경되면, 계약의 남은 세션을 차감
    if (status == PTAppointmentStatus.COMPLETED) {
      ptContractService.deductSession(appointment.getContract().getId());
    }
  }

  public Long propose(Long userId, AppointmentRequest request) {
    PTContract contract = ptContractRepository.findById(request.contractId())
        .orElseThrow(() -> new RestApiException(PTErrorCode.NOT_FOUND_PT_APPOINTMENT));

    // 권한 확인 (요청자가 계약의 회원인지)
    if (!contract.getMember().getId().equals(userId)) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }

    // 계약 상태 및 남은 세션 확인
    if (contract.getStatus() != PTContractStatus.ACTIVE) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
    }
    if (contract.getRemainingSessions() <= 0) {
      throw new RestApiException(PTErrorCode.NO_REMAIN_SESSION);
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
  public void confirm(Long userId, Long appointmentId) {
    PTAppointment appointment = ptAppointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new RestApiException(PTErrorCode.NOT_FOUND_PT_APPOINTMENT));

    // 권한 확인 (트레이너인지)
    if (!appointment.getContract().getTrainer().getId().equals(userId)) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }

    // 상태 확인
    if (appointment.getStatus() != PTAppointmentStatus.MEMBER_REQUESTED) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
    }

    appointment.setStatus(PTAppointmentStatus.SCHEDULED);
    ptAppointmentRepository.save(appointment);
  }

  public void requestChange(Long userId, Long appointmentId,
      AppointmentUpdateRequest request) {
    PTAppointment appointment = findAppointmentById(appointmentId);

    // 권한 확인: 본인(회원)의 예약인지 확인
    if (!appointment.getContract().getMember().getId().equals(userId)) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
    }
    // 상태 확인: 확정된 스케줄만 변경 요청 가능
    if (appointment.getStatus() != PTAppointmentStatus.SCHEDULED) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
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
  public void requestChangeByTrainer(Long userId, Long appointmentId,
      AppointmentUpdateRequest request) {
    PTAppointment appointment = findAppointmentById(appointmentId);

    // 권한 확인: 담당 트레이너인지 확인
    if (!appointment.getContract().getTrainer().getId().equals(userId)) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }
    // 상태 확인: 확정된 스케줄만 변경 요청 가능
    if (appointment.getStatus() != PTAppointmentStatus.SCHEDULED) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
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
  public void approveChange(Long userId, Long appointmentId) {
    PTAppointment appointment = findAppointmentById(appointmentId);

    // 권한 확인: 담당 트레이너인지 확인
    if (!appointment.getContract().getTrainer().getId().equals(userId)) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }
    // 상태 확인: 회원이 변경 요청한 상태인지 확인
    if (appointment.getStatus() != PTAppointmentStatus.CHANGE_REQUESTED) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
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
  public void approveChangeByMember(Long userId, Long appointmentId) {
    PTAppointment appointment = findAppointmentById(appointmentId);

    // 권한 확인: 예약의 당사자(회원)인지 확인
    if (!appointment.getContract().getMember().getId().equals(userId)) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }
    // 상태 확인: 트레이너가 변경 요청한 상태인지 확인
    if (appointment.getStatus() != PTAppointmentStatus.TRAINER_CHANGE_REQUESTED) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
    }

    // 제안된 시간으로 실제 예약 시간을 업데이트
    appointment.setStartTime(appointment.getProposedStartTime());
    appointment.setEndTime(appointment.getProposedEndTime());

    appointment.setProposedStartTime(null);
    appointment.setProposedEndTime(null);
    appointment.setStatus(PTAppointmentStatus.SCHEDULED); // 다시 '확정' 상태로 변경
  }

  /**
   * PT 스케줄 변경 요청을 거절합니다.
   */
  public void rejectChange(Long userId, Long appointmentId) {
    PTAppointment appointment = findAppointmentById(appointmentId);

    boolean isMember = userId.equals(appointment.getContract().getMember().getId());
    boolean isTrainer = userId.equals(appointment.getContract().getTrainer().getId());

    // 거절 권한 확인: 트레이너가 회원의 요청을 거절하거나, 회원이 트레이너의 요청을 거절하는 경우
    boolean canReject =
        (isTrainer && appointment.getStatus() == PTAppointmentStatus.CHANGE_REQUESTED) ||
            (isMember && appointment.getStatus() == PTAppointmentStatus.TRAINER_CHANGE_REQUESTED);

    if (!canReject) {
      throw new RestApiException(PTErrorCode.INVALID_STATUS_REQUEST);
    }

    // 제안 시간 필드만 초기화하고 원래 시간으로 되돌림
    appointment.setProposedStartTime(null);
    appointment.setProposedEndTime(null);
    appointment.setStatus(PTAppointmentStatus.SCHEDULED); // 다시 '확정' 상태로 변경
  }

  private PTAppointment findAppointmentById(Long appointmentId) {
    return ptAppointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new RestApiException(PTErrorCode.NOT_FOUND_PT_APPOINTMENT));
  }
}