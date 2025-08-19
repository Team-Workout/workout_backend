package com.workout.pt.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.pt.dto.AppointmentDetails;
import com.workout.pt.dto.AppointmentRequest;
import com.workout.pt.dto.AppointmentStatusUpdateRequest;
import com.workout.pt.dto.AppointmentUpdateRequest;
import com.workout.pt.service.PTAppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/pt-appointments") // 'PT 예약' 리소스를 관리하는 컨트롤러
public class PTAppointmentController {

  private final PTAppointmentService appointmentService;

  public PTAppointmentController(PTAppointmentService appointmentService) {
    this.appointmentService = appointmentService;
  }

  /**
   * 1. PT 스케줄 생성 (수업 예약)
   * - 사용자와 트레이너 모두 예약할 수 있어야 합니다.
   * - 요청 Body에 어떤 계약(contract_id)에 대한 예약인지 명시합니다.
   */
  @PostMapping
  public ResponseEntity<Void> createAppointment(
      @AuthenticationPrincipal UserPrincipal user,
      @RequestBody AppointmentRequest request // DTO: { "contractId": 101, "startTime": "...", "endTime": "..." }
  ) {
    Long appointmentId = appointmentService.create(user.getId(), request);

    // 생성 성공 시, 생성된 리소스의 URI를 Location 헤더에 담아 201 Created 응답
    URI location = URI.create("/api/pt-appointments/" + appointmentId);
    return ResponseEntity.created(location).build();
  }

  /**
   * 2. PT 스케줄 조회
   * - 이 API는 역할(사용자인지, 트레이너인지)에 따라 다른 Controller에 있을 수도 있습니다. (아래 추가 설명 참고)
   * - 특정 예약 한 건에 대한 상세 조회입니다.
   */
  @GetMapping("/{appointmentId}")
  public ResponseEntity<AppointmentDetails> getAppointment(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long appointmentId
  ) {
    AppointmentDetails appointment = appointmentService.findById(user.getId(), appointmentId);
    return ResponseEntity.ok(appointment);
  }

  /**
   * 3. PT 스케줄 수정 (시간 변경)
   * - 예약 시간을 변경합니다.
   */
  @PatchMapping("/{appointmentId}")
  public ResponseEntity<Void> updateAppointment(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long appointmentId,
      @RequestBody AppointmentUpdateRequest request // DTO: { "startTime": "...", "endTime": "..." }
  ) {
    appointmentService.update(user.getId(), appointmentId, request);
    return ResponseEntity.ok().build();
  }

  /**
   * 4. PT 스케줄 상태 변경 (e.g., 완료, 취소)
   * - 수업을 '완료(COMPLETED)' 처리하거나 '취소(CANCELED)' 합니다.
   */
  @PatchMapping("/{appointmentId}/status")
  public ResponseEntity<Void> updateAppointmentStatus(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long appointmentId,
      @RequestBody AppointmentStatusUpdateRequest request // DTO: { "status": "COMPLETED" }
  ) {
    appointmentService.updateStatus(user.getId(), appointmentId, request.getStatus());
    return ResponseEntity.ok().build();
  }
}