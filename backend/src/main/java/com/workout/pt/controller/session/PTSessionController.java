package com.workout.pt.controller.session;

import org.springframework.web.bind.annotation.RestController;

import com.workout.auth.domain.UserPrincipal;
import com.workout.pt.dto.request.PTSessionCreateRequest;
import com.workout.pt.service.session.PTSessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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

    Long ptSessionId = ptSessionService.createPTSession(request, user);
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