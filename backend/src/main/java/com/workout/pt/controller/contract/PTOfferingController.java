package com.workout.pt.controller.contract;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.dto.request.OfferingCreateRequest;
import com.workout.pt.dto.response.PtOfferingResponse;
import com.workout.pt.service.contract.PTOfferingService;
import java.util.List;
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
  public ResponseEntity<ApiResponse<Void>> createOffering( // [변경]
      @AuthenticationPrincipal UserPrincipal trainer,
      @RequestBody OfferingCreateRequest request) {
    Long userId = trainer.getUserId();
    offeringService.register(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.empty());
  }

  @GetMapping("/trainer/{trainerId}")
  public ResponseEntity<ApiResponse<List<PtOfferingResponse>>> getOfferingsByTrainer( // [변경]
      @PathVariable Long trainerId) {
    List<PtOfferingResponse> offerings = offeringService.findByTrainerId(trainerId);
    return ResponseEntity.ok(ApiResponse.of(offerings));
  }

  @DeleteMapping("/{offeringId}")
  public ResponseEntity<ApiResponse<Void>> deleteOffering( // [변경]
      @AuthenticationPrincipal UserPrincipal trainer,
      @PathVariable Long offeringId) {
    Long userId = trainer.getUserId();
    offeringService.delete(offeringId, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }
}
