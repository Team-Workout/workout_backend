package com.workout.pt.controller.contract;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.dto.request.PtApplicationRequest;
import com.workout.pt.dto.response.PendingApplicationResponse;
import com.workout.pt.service.contract.PTApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PT - 신청", description = "회원의 PT 신청 및 트레이너의 수락/거절 API")
@RestController
@RequestMapping("/api/pt-applications")
public class PTApplicationController {

  private final PTApplicationService ptApplicationService;

  public PTApplicationController(PTApplicationService ptApplicationService) {
    this.ptApplicationService = ptApplicationService;
  }

  @Operation(summary = "대기 중인 PT 신청 내역 조회 (공통)",
      description = "로그인한 사용자(회원/트레이너) 기준으로 보낸 신청 또는 받은 신청 중 '대기중(PENDING)'인 내역을 조회합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  @GetMapping
  public ResponseEntity<ApiResponse<PendingApplicationResponse>> findPendingApplications(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal.getUserId();
    PendingApplicationResponse response = ptApplicationService.findPendingApplicationsForUser(
        userId);
    return ResponseEntity.ok(ApiResponse.of(response));
  }

  @Operation(summary = "[회원] PT 신청 생성",
      description = "로그인한 회원(MEMBER)이 특정 PT 오퍼링(offeringId)에 대해 PT를 신청합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "PT 신청 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류 (오퍼링 ID 누락)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않았거나 회원이 아님"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "오퍼링을 찾을 수 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 신청했거나 진행중인 계약이 있음 (Conflict)")
  })
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createApplication(
      @AuthenticationPrincipal UserPrincipal user,
      @Valid @RequestBody PtApplicationRequest request
  ) {
    Long userId = user.getUserId();
    ptApplicationService.createApplication(request, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @Operation(summary = "[트레이너] PT 신청 수락",
      description = "로그인한 트레이너가 본인에게 온 PT 신청을 수락합니다. 수락 시 활성 PT 계약이 생성됩니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신청 수락 및 계약 생성 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않았거나 트레이너가 아님"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인에게 온 신청이 아님 (권한 없음)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신청 내역을 찾을 수 없음")
  })
  @PatchMapping("/{applicationId}/acceptance")
  public ResponseEntity<ApiResponse<Void>> acceptApplication(
      @AuthenticationPrincipal UserPrincipal trainer,
      @Parameter(description = "수락할 신청 ID", required = true) @PathVariable Long applicationId
  ) {
    Long userId = trainer.getUserId();
    ptApplicationService.acceptApplication(applicationId, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @Operation(summary = "[트레이너] PT 신청 거절",
      description = "로그인한 트레이너가 본인에게 온 PT 신청을 거절합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신청 거절 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않았거나 트레이너가 아님"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인에게 온 신청이 아님 (권한 없음)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신청 내역을 찾을 수 없음")
  })
  @PatchMapping("/{applicationId}/rejection")
  public ResponseEntity<ApiResponse<Void>> rejectApplication(
      @AuthenticationPrincipal UserPrincipal trainer,
      @Parameter(description = "거절할 신청 ID", required = true) @PathVariable Long applicationId
  ) {
    Long userId = trainer.getUserId();
    ptApplicationService.rejectApplication(applicationId, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @Operation(summary = "[회원] PT 신청 취소",
      description = "로그인한 회원이 본인이 신청했던 PT 신청(아직 트레이너가 수락하지 않은 건)을 취소합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신청 취소 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않았거나 회원이 아님"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인이 신청한 내역이 아님 (권한 없음)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신청 내역을 찾을 수 없음")
  })
  @PatchMapping("/{applicationId}/cancellation")
  public ResponseEntity<ApiResponse<Void>> cancelApplication(
      @AuthenticationPrincipal UserPrincipal user,
      @Parameter(description = "취소할 신청 ID", required = true) @PathVariable Long applicationId
  ) {
    Long userId = user.getUserId();
    ptApplicationService.cancelApplication(applicationId, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }
}