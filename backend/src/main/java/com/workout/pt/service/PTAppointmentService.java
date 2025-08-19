package com.workout.pt.service;


import com.workout.auth.domain.UserPrincipal;
import com.workout.pt.domain.appointment.PTAppointment;
import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.dto.request.AppointmentRequest;
import com.workout.pt.repository.PTAppointmentRepository;
import com.workout.pt.repository.PTContractRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PTAppointmentService {

  private final PTAppointmentRepository ptAppointmentRepository;
  private final PTContractRepository ptContractRepository;
  private final PTContractService ptContractService; // 세션 차감을 위해 주입

  @Transactional
  public Long create(Long userId, AppointmentRequest request) {
    // 1. 예약을 요청한 계약(Contract)이 유효한지 확인
    PTContract contract = ptContractRepository.findById(request.contractId())
        .orElseThrow(() -> new EntityNotFoundException("계약 정보를 찾을 수 없습니다."));

    // TODO: 2. 본인의 계약인지, 트레이너의 계약인지 권한 확인 로직 추가
    // TODO: 3. 남은 세션(remainingSessions)이 0보다 큰지 확인
    // TODO: 4. 요청된 시간에 다른 예약과 겹치지 않는지 확인 (스케줄 중복 체크)

    PTAppointment appointment = PTAppointment.builder()
        .contract(contract)
        .startTime(request.startTime())
        .endTime(request.endTime())
        .status(PTAppointmentStatus.SCHEDULED) // 초기 상태는 '예약됨'
        .build();

    return ptAppointmentRepository.save(appointment).getId();
  }

  @Transactional
  public void updateStatus(Long userId, Long appointmentId, PTAppointmentStatus status) {
    PTAppointment appointment = ptAppointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다."));

    // TODO: 상태를 변경할 권한이 있는지 확인

    appointment.setStatus(status);

    // **매우 중요**: 수업 상태가 '완료(COMPLETED)'로 변경되면, 계약의 남은 세션을 차감해야 합니다.
    if (status == PTAppointmentStatus.COMPLETED) {
      ptContractService.deductSession(appointment.getContract().getId());
    }
  }
  // ... findById, update 등 다른 메서드 구현 ...
}