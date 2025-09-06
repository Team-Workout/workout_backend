package com.workout.pt.controller.contract;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.domain.contract.PTAppointmentStatus;
import com.workout.pt.dto.request.AppointmentRequest;
import com.workout.pt.dto.request.AppointmentStatusUpdateRequest;
import com.workout.pt.dto.request.AppointmentUpdateRequest;
import com.workout.pt.dto.response.AppointmentResponse;
import com.workout.pt.service.contract.PTAppointmentService;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pt-appointments") // 'PT 예약' 리소스를 관리하는 컨트롤러
public class PTAppointmentController {

  private final PTAppointmentService appointmentService;

  public PTAppointmentController(PTAppointmentService appointmentService) {
    this.appointmentService = appointmentService;
  }

  @GetMapping("/me/scheduled")
  public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getMyScheduledAppointments(
      @AuthenticationPrincipal UserPrincipal user,
      @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam("status") PTAppointmentStatus status

  ) {
    Long userId = user.getUserId();
    List<AppointmentResponse> appointments = appointmentService.findMyScheduledAppointmentsByPeriod(
        userId, startDate, endDate, status);

    return ResponseEntity.ok(ApiResponse.of(appointments));
  }
  //region 트레이너

  @PostMapping
  public ResponseEntity<ApiResponse<Long>> createAppointment(
      @AuthenticationPrincipal UserPrincipal user,
      @RequestBody AppointmentRequest request
  ) {
    Long userId = user.getUserId();
    Long appointmentId = appointmentService.create(userId, request);
    URI location = URI.create("/api/pt-appointments/" + appointmentId);
    return ResponseEntity.created(location).body(ApiResponse.of(appointmentId));
  }

  @PatchMapping("/{appointmentId}/confirm")
  public ResponseEntity<ApiResponse<Void>> confirmAppointment(
      @AuthenticationPrincipal UserPrincipal trainer,
      @PathVariable Long appointmentId
  ) {
    Long userId = trainer.getUserId();
    appointmentService.confirm(userId, appointmentId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @PatchMapping("/{appointmentId}/trainer-change-request")
  public ResponseEntity<ApiResponse<Void>> requestChangeByTrainer(
      @AuthenticationPrincipal UserPrincipal trainer,
      @PathVariable Long appointmentId,
      @RequestBody AppointmentUpdateRequest request
  ) {
    Long userId = trainer.getUserId();
    appointmentService.requestChangeByTrainer(userId, appointmentId, request);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @PatchMapping("/{appointmentId}/status")
  public ResponseEntity<ApiResponse<Void>> updateAppointmentStatus(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long appointmentId,
      @RequestBody AppointmentStatusUpdateRequest request
  ) {
    Long userId = user.getUserId();
    appointmentService.updateStatus(userId, appointmentId, request.status());
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @PatchMapping("/{appointmentId}/change-approval")
  public ResponseEntity<ApiResponse<Void>> approveAppointmentChange(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long appointmentId
  ) {
    Long userId = user.getUserId();
    appointmentService.approveChange(userId, appointmentId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @PatchMapping("/{appointmentId}/change-rejection")
  public ResponseEntity<ApiResponse<Void>> rejectAppointmentChange(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long appointmentId
  ) {
    Long userId = user.getUserId();
    appointmentService.rejectChange(userId, appointmentId);
    return ResponseEntity.ok(ApiResponse.empty());
  }
  //endregion

  //region 회원

  @PostMapping("/propose")
  public ResponseEntity<ApiResponse<Long>> proposeAppointment(
      @AuthenticationPrincipal UserPrincipal user,
      @RequestBody AppointmentRequest request
  ) {
    Long userId = user.getUserId();
    Long appointmentId = appointmentService.propose(userId, request);
    URI location = URI.create("/api/pt-appointments/" + appointmentId);
    return ResponseEntity.created(location).body(ApiResponse.of(appointmentId));
  }

  @PatchMapping("/{appointmentId}/member-approve-change")
  public ResponseEntity<ApiResponse<Void>> approveChangeByMember(
      @AuthenticationPrincipal UserPrincipal member,
      @PathVariable Long appointmentId
  ) {
    Long userId = member.getUserId();
    appointmentService.approveChangeByMember(userId, appointmentId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @PatchMapping("/{appointmentId}/change-request")
  public ResponseEntity<ApiResponse<Void>> requestAppointmentChange(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long appointmentId,
      @RequestBody AppointmentUpdateRequest request
  ) {
    Long userId = user.getUserId();
    appointmentService.requestChange(userId, appointmentId, request);
    return ResponseEntity.ok(ApiResponse.empty());
  }
  //endregion
}