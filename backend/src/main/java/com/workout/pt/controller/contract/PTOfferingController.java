package com.workout.pt.controller.contract;

import com.workout.auth.domain.UserPrincipal;
import com.workout.pt.dto.request.OfferingCreateRequest;
import com.workout.pt.dto.response.PtOfferingResponse;
import com.workout.pt.service.contract.PTOfferingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pt-offerings")
public class PTOfferingController {

  private final PTOfferingService offeringService;

  public PTOfferingController(PTOfferingService offeringService) {
    this.offeringService = offeringService;
  }

  @PostMapping
  public ResponseEntity<Void> createOffering(
      @AuthenticationPrincipal UserPrincipal trainer,
      @RequestBody OfferingCreateRequest request) {
    Long userId = trainer.getUserId();
    offeringService.register(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/trainer/{trainerId}")
  public ResponseEntity<PtOfferingResponse> getOfferingsByTrainer(
      @PathVariable Long trainerId) {
    return ResponseEntity.ok(offeringService.findbyTrainerId(trainerId));
  }

  @DeleteMapping("/{offeringId}")
  public ResponseEntity<Void> deleteOffering(
      @AuthenticationPrincipal UserPrincipal trainer,
      @PathVariable Long offeringId
  ) {
    Long userId = trainer.getUserId();
    offeringService.delete(offeringId, userId);
    return ResponseEntity.noContent().build();
  }
}
