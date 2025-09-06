package com.workout.pt.controller.contract;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.dto.request.PtApplicationRequest;
import com.workout.pt.dto.response.PendingApplicationResponse;
import com.workout.pt.service.contract.PTApplicationService;
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

@RestController
@RequestMapping("/api/pt-applications")
public class PTApplicationController {

  private final PTApplicationService ptApplicationService;

  public PTApplicationController(PTApplicationService ptApplicationService) {
    this.ptApplicationService = ptApplicationService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PendingApplicationResponse>> findPendingApplications( // [변경]
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal.getUserId();
    PendingApplicationResponse response = ptApplicationService.findPendingApplicationsForUser(userId);
    return ResponseEntity.ok(ApiResponse.of(response));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createApplication( // [변경]
      @AuthenticationPrincipal UserPrincipal user,
      @Valid @RequestBody PtApplicationRequest request
  ) {
    Long userId = user.getUserId();
    ptApplicationService.createApplication(request, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  /**
   * PT 신청 수락 (트레이너)
   */
  @PatchMapping("/{applicationId}/acceptance")
  public ResponseEntity<ApiResponse<Void>> acceptApplication(
      @AuthenticationPrincipal UserPrincipal trainer,
      @PathVariable Long applicationId
  ) {
    Long userId = trainer.getUserId();
    ptApplicationService.acceptApplication(applicationId, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  /**
   * PT 신청 거절 (트레이너)
   */
  @PatchMapping("/{applicationId}/rejection")
  public ResponseEntity<ApiResponse<Void>> rejectApplication(
      @AuthenticationPrincipal UserPrincipal trainer,
      @PathVariable Long applicationId
  ) {
    Long userId = trainer.getUserId();
    ptApplicationService.rejectApplication(applicationId, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  /**
   * PT 신청 취소 (유저)
   */
  @PatchMapping("/{applicationId}/cancellation")
  public ResponseEntity<ApiResponse<Void>> cancelApplication(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long applicationId
  ) {
    Long userId = user.getUserId();
    ptApplicationService.cancelApplication(applicationId, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

}
