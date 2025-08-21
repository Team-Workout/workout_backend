package com.workout.pt.controller.session;

import com.workout.auth.domain.UserPrincipal;
import com.workout.pt.dto.request.PTSessionCreateRequest;
import com.workout.pt.service.session.PTSessionService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pt-sessions")
public class PTSessionController {

  private final PTSessionService ptSessionService;

  public PTSessionController(PTSessionService ptSessionService) {
    this.ptSessionService = ptSessionService;
  }

  @PostMapping
  public ResponseEntity<Void> createPTSession(
      @AuthenticationPrincipal UserPrincipal user,
      @Valid @RequestBody PTSessionCreateRequest request) {

    Long ptSessionId = ptSessionService.createPTSessionAndWorkoutLog(request, user);
    return ResponseEntity.created(URI.create("/api/pt-sessions/" + ptSessionId)).build();
  }

  @DeleteMapping("/{ptSessionId}")
  public ResponseEntity<Void> deletePTSession(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long ptSessionId) {

    ptSessionService.deletePTSession(ptSessionId, user);
    return ResponseEntity.noContent().build();
  }
}