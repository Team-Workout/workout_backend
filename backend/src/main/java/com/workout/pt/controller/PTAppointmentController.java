package com.workout.pt.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.pt.dto.request.AppointmentRequest;
import com.workout.pt.dto.request.AppointmentStatusUpdateRequest;
import com.workout.pt.dto.request.AppointmentUpdateRequest;
import com.workout.pt.dto.response.AppointmentResponse;
import com.workout.pt.service.PTAppointmentService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pt-appointments") // 'PT 예약' 리소스를 관리하는 컨트롤러
public class PTAppointmentController {

  private final PTAppointmentService appointmentService;

  public PTAppointmentController(PTAppointmentService appointmentService) {
    this.appointmentService = appointmentService;
  }

  /**
   * PT 스케줄 조회
   */
  @GetMapping("/me/scheduled")
  public ResponseEntity<AppointmentResponse> getMyScheduledAppointments(
      @AuthenticationPrincipal UserPrincipal user
  ) {
    AppointmentResponse appointments = appointmentService.findMyScheduledAppointments(user);
    return ResponseEntity.ok(appointments);
  }

  //region 트레이너
  /**
   * PT 스케줄 생성 (트레이너)
   */
  @PostMapping
  public ResponseEntity<Void> createAppointment(
      @AuthenticationPrincipal UserPrincipal user,
      @RequestBody AppointmentRequest request
  ) {
    Long appointmentId = appointmentService.create(user, request);

    return ResponseEntity.ok().build();
  }

  /**
   * 회원의 제안을 확정
   */
  @PatchMapping("/{appointmentId}/confirm")
  public ResponseEntity<Void> confirmAppointment(
      @AuthenticationPrincipal UserPrincipal trainer,
      @PathVariable Long appointmentId
  ) {
    appointmentService.confirm(trainer, appointmentId);
    return ResponseEntity.ok().build();
  }

  /**
   * 스케줄 변경을 요청
   */
  @PatchMapping("/{appointmentId}/trainer-change-request")
  public ResponseEntity<Void> requestChangeByTrainer(
      @AuthenticationPrincipal UserPrincipal trainer,
      @PathVariable Long appointmentId,
      @RequestBody AppointmentUpdateRequest request
  ) {
    appointmentService.requestChangeByTrainer(trainer, appointmentId, request);
    return ResponseEntity.ok().build();
  }

  /**
   * PT 스케줄 상태 변경 (트레이너)
   */
  @PatchMapping("/{appointmentId}/status")
  public ResponseEntity<Void> updateAppointmentStatus(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long appointmentId,
      @RequestBody AppointmentStatusUpdateRequest request
  ) {
    appointmentService.updateStatus(user, appointmentId, request.status());
    return ResponseEntity.ok().build();
  }

  /**
   * PT 스케줄 변경 수락 (트레이너)
   */
  @PatchMapping("/{appointmentId}/change-approval")
  public ResponseEntity<Void> approveAppointmentChange(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long appointmentId
  ) {
    appointmentService.approveChange(user, appointmentId);
    return ResponseEntity.ok().build();
  }

  /**
   * PT 스케줄 변경 거절 (트레이너)
   */
  @PatchMapping("/{appointmentId}/change-rejection")
  public ResponseEntity<Void> rejectAppointmentChange(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long appointmentId
  ) {
    appointmentService.rejectChange(user, appointmentId);
    return ResponseEntity.ok().build();
  }
  //endregion

  //region 회원
  /**
   * 회원이 PT 희망 시간을 제안
   */
  @PostMapping("/propose")
  public ResponseEntity<Void> proposeAppointment(
      @AuthenticationPrincipal UserPrincipal user,
      @RequestBody AppointmentRequest request
  ) {
    Long appointmentId = appointmentService.propose(user, request);
    // 생성된 리소스의 위치를 헤더에 담아 반환 (RESTful Best Practice)
    return ResponseEntity.created(URI.create("/api/pt-appointments/" + appointmentId)).build();
  }

  /**
   * 회원이 트레이너의 변경 요청을 수락
   */
  @PatchMapping("/{appointmentId}/member-approve-change")
  public ResponseEntity<Void> approveChangeByMember(
      @AuthenticationPrincipal UserPrincipal member,
      @PathVariable Long appointmentId
  ) {
    appointmentService.approveChangeByMember(member, appointmentId);
    return ResponseEntity.ok().build();
  }

  /**
   * PT 스케줄 변경 신청 (회원)
   */
  @PatchMapping("/{appointmentId}/change-request")
  public ResponseEntity<Void> requestAppointmentChange(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long appointmentId,
      @RequestBody AppointmentUpdateRequest request // DTO: { "startTime": "...", "endTime": "..." }
  ) {
    appointmentService.requestChange(user, appointmentId, request);
    return ResponseEntity.ok().build();
  }
  //endregion
}