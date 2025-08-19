package com.workout.pt.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.pt.dto.PendingApplicationResponse;
import com.workout.pt.dto.PtApplicationRequest;
import com.workout.pt.service.PTApplicationService;
import com.workout.pt.service.PTContractService;
import com.workout.trainer.domain.Trainer;
import org.springframework.http.HttpStatus;
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

  private final PTContractService ptContractService;
  private final PTApplicationService ptApplicationService;

  public PTApplicationController(PTContractService ptContractService, PTApplicationService ptApplicationService) {
    this.ptContractService = ptContractService;
    this.ptApplicationService = ptApplicationService;
  }

  /**
   * PT신청 조회 (트레이너, 회원)
   */
  @GetMapping
  public ResponseEntity<PendingApplicationResponse> findPendingApplications(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    return ResponseEntity.ok(ptApplicationService.findPendingApplicationsForUser(userPrincipal));
  }

  /**
   * PT 신청 수락 (트레이너)
   */
  @PatchMapping("/{applicationId}/acceptance")
  public ResponseEntity<Void> acceptApplication(
      @AuthenticationPrincipal UserPrincipal trainer, // 트레이너로 로그인한 사용자
      @PathVariable Long applicationId
  ) {
    ptApplicationService.acceptApplication(applicationId, trainer);
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
    ptApplicationService.rejectApplication(applicationId, trainer);
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
    ptApplicationService.cancelApplication(applicationId, user);
    return ResponseEntity.ok().build();
  }

}
