package com.workout.pt.controller.session;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.dto.request.PTSessionCreateRequest;
import com.workout.pt.dto.response.PTSessionResponse;
import com.workout.pt.service.session.PTSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "PT - 세션 (수업 기록)", description = "PT 수업 완료 후 생성되는 세션 기록 API")
@RestController
@RequestMapping("/api/pt-sessions")
public class PTSessionController {

  private final PTSessionService ptSessionService;

  public PTSessionController(PTSessionService ptSessionService) {
    this.ptSessionService = ptSessionService;
  }

  @Operation(summary = "[트레이너] PT 세션 생성 (수업 기록)",
      description = "PT 수업 완료 후, 트레이너가 수업 세션 기록을 생성합니다. 이 때 PT 계약 횟수가 차감되며, 연관된 운동일지(WorkoutLog)도 함께 생성됩니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "PT 세션 생성 성공 (생성된 ID 반환)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패 또는 PT 잔여 횟수 부족"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않았거나 트레이너가 아님"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 계약이 아님 (권한 없음)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "관련된 계약 또는 회원을 찾을 수 없음")
  })
  @PostMapping
  public ResponseEntity<ApiResponse<Long>> createPTSession(
      @AuthenticationPrincipal UserPrincipal user,
      @Valid @RequestBody PTSessionCreateRequest request) {

    Long userId = user.getUserId();
    Long ptSessionId = ptSessionService.createPTSessionAndWorkoutLog(request, userId);
    URI location = URI.create("/api/pt-sessions/" + ptSessionId);

    return ResponseEntity.created(location).body(ApiResponse.of(ptSessionId));
  }

  @Operation(summary = "PT 세션 삭제 (공통)",
      description = "로그인한 사용자가 본인과 관련된 PT 세션 기록을 삭제합니다. (연관된 운동일지도 함께 삭제되며, 차감되었던 PT 횟수가 복구됩니다.)",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "세션 삭제 및 PT 횟수 복구 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 세션이 아님 (삭제 권한 없음)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
  })
  @DeleteMapping("/{ptSessionId}")
  public ResponseEntity<ApiResponse<Void>> deletePTSession(
      @AuthenticationPrincipal UserPrincipal user,
      @Parameter(description = "삭제할 PT 세션 ID", required = true) @PathVariable Long ptSessionId) {

    Long userId = user.getUserId();
    ptSessionService.deletePTSession(ptSessionId, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @Operation(summary = "내 PT 세션 목록 조회 (공통)",
      description = "로그인한 사용자(회원 또는 트레이너) 본인과 관련된 모든 PT 세션 기록을 페이징하여 조회합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 세션 목록 조회 성공 (페이징)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
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