package com.workout.pt.controller.contract;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.domain.contract.PTAppointmentStatus;
import com.workout.pt.dto.request.AppointmentRequest;
import com.workout.pt.dto.request.AppointmentStatusUpdateRequest;
import com.workout.pt.dto.request.AppointmentUpdateRequest;
import com.workout.pt.dto.response.AppointmentResponse;
import com.workout.pt.service.contract.PTAppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "PT - 수업 예약", description = "PT 계약 기반 수업 예약 및 상태 변경 API")
@RestController
@RequestMapping("/api/pt-appointments") // 'PT 예약' 리소스를 관리하는 컨트롤러
public class PTAppointmentController {

  private final PTAppointmentService appointmentService;

  public PTAppointmentController(PTAppointmentService appointmentService) {
    this.appointmentService = appointmentService;
  }

  @Operation(summary = "내 예약 일정 조회 (공통, 기간/상태별)",
      description = "로그인한 사용자(회원/트레이너)의 특정 기간 및 상태에 해당하는 모든 예약 일정을 조회합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일정 조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  @GetMapping("/me/scheduled")
  public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getMyScheduledAppointments(
      @AuthenticationPrincipal UserPrincipal user,
      @Parameter(description = "조회 시작일", required = true, example = "2025-01-01") @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @Parameter(description = "조회 종료일", required = true, example = "2025-01-31") @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @Parameter(description = "조회할 예약 상태 (예: SCHEDULED, PENDING, COMPLETED 등)", required = true) @RequestParam("status") PTAppointmentStatus status

  ) {
    Long userId = user.getUserId();
    List<AppointmentResponse> appointments = appointmentService.findMyScheduledAppointmentsByPeriod(
        userId, startDate, endDate, status);

    return ResponseEntity.ok(ApiResponse.of(appointments));
  }
  //region 트레이너

  @Operation(summary = "[트레이너] PT 수업 생성 및 확정",
      description = "트레이너가 직접 회원의 수업 일정을 생성하고 등록합니다 (생성 즉시 'SCHEDULED(확정)' 상태가 됨).",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "수업 생성 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않았거나 트레이너가 아님"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 담당 회원이 아님 (권한 없음)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "관련된 계약/회원/짐 정보를 찾을 수 없음")
  })
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

  @Operation(summary = "[트레이너] 회원의 수업 제안 확정",
      description = "회원이 제안(PENDING 상태)한 수업 예약을 트레이너가 'SCHEDULED(확정)' 상태로 변경합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수업 확정 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 트레이너"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인에게 온 예약 제안이 아님 (권한 없음)")
  })
  @PatchMapping("/{appointmentId}/confirm")
  public ResponseEntity<ApiResponse<Void>> confirmAppointment(
      @AuthenticationPrincipal UserPrincipal trainer,
      @Parameter(description = "확정할 예약 ID", required = true) @PathVariable Long appointmentId
  ) {
    Long userId = trainer.getUserId();
    appointmentService.confirm(userId, appointmentId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @Operation(summary = "[트레이너] 수업 일정 변경 요청",
      description = "트레이너가 확정된 수업의 일정 변경을 회원에게 요청합니다. (상태: CHANGE_REQUESTED_BY_TRAINER)",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 요청 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 트레이너"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 수업이 아님 (권한 없음)")
  })
  @PatchMapping("/{appointmentId}/trainer-change-request")
  public ResponseEntity<ApiResponse<Void>> requestChangeByTrainer(
      @AuthenticationPrincipal UserPrincipal trainer,
      @Parameter(description = "변경 요청할 예약 ID", required = true) @PathVariable Long appointmentId,
      @RequestBody AppointmentUpdateRequest request
  ) {
    Long userId = trainer.getUserId();
    appointmentService.requestChangeByTrainer(userId, appointmentId, request);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @Operation(summary = "수업 상태 변경 (공통)",
      description = "수업의 상태를 변경합니다 (예: COMPLETED, MEMBER_ABSENT, TRAINER_CANCELLED 등). 트레이너 또는 회원이 권한에 따라 사용합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 변경 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 수업이 아니거나 상태 변경 권한이 없음")
  })
  @PatchMapping("/{appointmentId}/status")
  public ResponseEntity<ApiResponse<Void>> updateAppointmentStatus(
      @AuthenticationPrincipal UserPrincipal user,
      @Parameter(description = "상태 변경할 예약 ID", required = true) @PathVariable Long appointmentId,
      @RequestBody AppointmentStatusUpdateRequest request
  ) {
    Long userId = user.getUserId();
    appointmentService.updateStatus(userId, appointmentId, request.status());
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @Operation(summary = "수업 변경 승인 (공통)",
      description = "상대방(회원 또는 트레이너)이 요청한 일정 변경을 승인합니다. (상태: SCHEDULED)",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 승인 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "변경 승인 권한이 없음")
  })
  @PatchMapping("/{appointmentId}/change-approval")
  public ResponseEntity<ApiResponse<Void>> approveAppointmentChange(
      @AuthenticationPrincipal UserPrincipal user,
      @Parameter(description = "변경 승인할 예약 ID", required = true) @PathVariable Long appointmentId
  ) {
    Long userId = user.getUserId();
    appointmentService.approveChange(userId, appointmentId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @Operation(summary = "수업 변경 거절 (공통)",
      description = "상대방(회원 또는 트레이너)이 요청한 일정 변경을 거절합니다. (상태: SCHEDULED로 복귀)",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 거절 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "변경 거절 권한이 없음")
  })
  @PatchMapping("/{appointmentId}/change-rejection")
  public ResponseEntity<ApiResponse<Void>> rejectAppointmentChange(
      @AuthenticationPrincipal UserPrincipal user,
      @Parameter(description = "변경 거절할 예약 ID", required = true) @PathVariable Long appointmentId
  ) {
    Long userId = user.getUserId();
    appointmentService.rejectChange(userId, appointmentId);
    return ResponseEntity.ok(ApiResponse.empty());
  }
  //endregion

  //region 회원
  @Operation(summary = "[회원] PT 수업 제안",
      description = "회원이 트레이너에게 원하는 수업 일정을 제안(요청)합니다 (생성 시 'PENDING' 상태).",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "수업 제안 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 회원"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 계약이 아니거나 PT 횟수 소진 (권한 없음)")
  })
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

  @Operation(summary = "[회원] 트레이너의 수업 변경 요청 승인",
      description = "트레이너가 요청한 일정 변경을 회원이 승인합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 승인 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 회원"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "승인 권한이 없음")
  })
  @PatchMapping("/{appointmentId}/member-approve-change")
  public ResponseEntity<ApiResponse<Void>> approveChangeByMember(
      @AuthenticationPrincipal UserPrincipal member,
      @Parameter(description = "승인할 예약 ID", required = true) @PathVariable Long appointmentId
  ) {
    Long userId = member.getUserId();
    appointmentService.approveChangeByMember(userId, appointmentId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @Operation(summary = "[회원] 수업 일정 변경 요청",
      description = "회원이 확정된 수업의 일정 변경을 트레이너에게 요청합니다. (상태: CHANGE_REQUESTED_BY_MEMBER)",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 요청 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 회원"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 수업이 아님 (권한 없음)")
  })
  @PatchMapping("/{appointmentId}/change-request")
  public ResponseEntity<ApiResponse<Void>> requestAppointmentChange(
      @AuthenticationPrincipal UserPrincipal user,
      @Parameter(description = "변경 요청할 예약 ID", required = true) @PathVariable Long appointmentId,
      @RequestBody AppointmentUpdateRequest request
  ) {
    Long userId = user.getUserId();
    appointmentService.requestChange(userId, appointmentId, request);
    return ResponseEntity.ok(ApiResponse.empty());
  }
  //endregion
}