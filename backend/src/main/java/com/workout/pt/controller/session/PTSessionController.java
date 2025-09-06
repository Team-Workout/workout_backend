package com.workout.pt.controller.session;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.dto.request.PTSessionCreateRequest;
import com.workout.pt.dto.response.PTSessionResponse;
import com.workout.pt.service.session.PTSessionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/pt-sessions")
public class PTSessionController {

  private final PTSessionService ptSessionService;

  public PTSessionController(PTSessionService ptSessionService) {
    this.ptSessionService = ptSessionService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<Long>> createPTSession(
      @AuthenticationPrincipal UserPrincipal user,
      @Valid @RequestBody PTSessionCreateRequest request) {

    Long userId = user.getUserId();
    Long ptSessionId = ptSessionService.createPTSessionAndWorkoutLog(request, userId);
    URI location = URI.create("/api/pt-sessions/" + ptSessionId);

    return ResponseEntity.created(location).body(ApiResponse.of(ptSessionId));
  }

  @DeleteMapping("/{ptSessionId}")
  public ResponseEntity<ApiResponse<Void>> deletePTSession(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long ptSessionId) {

    Long userId = user.getUserId();
    ptSessionService.deletePTSession(ptSessionId, userId);

    return ResponseEntity.ok(ApiResponse.empty());
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<List<PTSessionResponse>>> getMyPTSession(
      @AuthenticationPrincipal UserPrincipal user,
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    Long userId = user.getUserId();
    Page<PTSessionResponse> responses = ptSessionService.findMySession(userId, pageable);
    return ResponseEntity.ok(ApiResponse.of(responses));
  }

}