package com.workout.pt.controller.contract;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.dto.request.OfferingCreateRequest;
import com.workout.pt.dto.response.PtOfferingResponse;
import com.workout.pt.service.contract.PTOfferingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "PT - 오퍼링 (상품)", description = "트레이너가 PT 상품(오퍼링)을 등록, 조회, 삭제하는 API")
@RestController
@RequestMapping("/api/pt-offerings")
public class PTOfferingController {

  private final PTOfferingService offeringService;

  public PTOfferingController(PTOfferingService offeringService) {
    this.offeringService = offeringService;
  }

  @Operation(summary = "[트레이너] PT 오퍼링(상품) 등록",
      description = "로그인한 트레이너가 본인의 PT 상품(예: 10회 50만원)을 등록합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "오퍼링 등록 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않았거나 트레이너가 아님")
  })
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createOffering(
      @AuthenticationPrincipal UserPrincipal trainer,
      @RequestBody OfferingCreateRequest request) { //
    Long userId = trainer.getUserId();
    offeringService.register(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.empty());
  }

  @Operation(summary = "특정 트레이너의 PT 오퍼링 목록 조회 (공개)",
      description = "특정 trainerId를 가진 트레이너가 등록한 모든 PT 상품 목록을 조회합니다.")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "오퍼링 목록 조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "트레이너를 찾을 수 없음")
  })
  @GetMapping("/trainer/{trainerId}")
  public ResponseEntity<ApiResponse<List<PtOfferingResponse>>> getOfferingsByTrainer(
      @Parameter(description = "오퍼링을 조회할 트레이너 ID", required = true) @PathVariable Long trainerId) {
    List<PtOfferingResponse> offerings = offeringService.findByTrainerId(trainerId);
    return ResponseEntity.ok(ApiResponse.of(offerings));
  }

  @Operation(summary = "[트레이너] PT 오퍼링(상품) 삭제",
      description = "로그인한 트레이너가 본인이 등록한 오퍼링을 삭제합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않았거나 트레이너가 아님"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인이 등록한 오퍼링이 아님 (삭제 권한 없음)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "오퍼링을 찾을 수 없음")
  })
  @DeleteMapping("/{offeringId}")
  public ResponseEntity<ApiResponse<Void>> deleteOffering(
      @AuthenticationPrincipal UserPrincipal trainer,
      @Parameter(description = "삭제할 오퍼링 ID", required = true) @PathVariable Long offeringId) {
    Long userId = trainer.getUserId();
    offeringService.delete(offeringId, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }
}