package com.workout.pt.controller.contract;

import com.workout.auth.domain.UserPrincipal;
import com.workout.pt.dto.request.PtApplicationRequest;
import com.workout.pt.dto.response.PendingApplicationResponse;
import com.workout.pt.service.contract.PTApplicationService;
import com.workout.pt.service.contract.PTContractService;
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

  /**
   * PT 신청 조회 (트레이너, 회원)
   */
  @GetMapping
  public ResponseEntity<PendingApplicationResponse> findPendingApplications(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    return ResponseEntity.ok(ptApplicationService.findPendingApplicationsForUser(userId));
  }

  /**
   * PT 신청
   */
  @PostMapping
  public ResponseEntity<Void> createApplication(
      @AuthenticationPrincipal UserPrincipal user,
      @Valid @RequestBody PtApplicationRequest request
  ) {
    Long userId = user.getUserId();
    ptApplicationService.createApplication(request, userId);
    return ResponseEntity.ok().build();
  }

  /**
   * PT 신청 수락 (트레이너)
   */
  @PatchMapping("/{applicationId}/acceptance")
  public ResponseEntity<Void> acceptApplication(
      @AuthenticationPrincipal UserPrincipal trainer,
      @PathVariable Long applicationId
  ) {
    Long userId = trainer.getUserId();
    ptApplicationService.acceptApplication(applicationId, userId);
    return ResponseEntity.ok().build();
  }

  /**
   * PT 신청 거절 (트레이너)
   */
  @PatchMapping("/{applicationId}/rejection")
  public ResponseEntity<Void> rejectApplication(
      @AuthenticationPrincipal UserPrincipal trainer,
      @PathVariable Long applicationId
  ) {
    Long userId = trainer.getUserId();
    ptApplicationService.rejectApplication(applicationId, userId);
    return ResponseEntity.ok().build();
  }

  /**
   * PT 신청 취소 (유저)
   */
  @PatchMapping("/{applicationId}/cancellation")
  public ResponseEntity<Void> cancelApplication(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long applicationId
  ) {
    Long userId = user.getUserId();
    ptApplicationService.cancelApplication(applicationId, userId);
    return ResponseEntity.ok().build();
  }

}
