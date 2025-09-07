// BodyCompositionController.java

package com.workout.body.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.body.dto.BodyCompositionDto;
import com.workout.body.dto.BodyCompositionResponse;
import com.workout.body.service.BodyCompositionService;
import com.workout.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "체성분 (Body Composition)", description = "사용자의 체성분(체중, 체지방, 근육량) 데이터 CRUD API")
@RestController
@RequestMapping("/api/body")
public class BodyCompositionController {

  private final BodyCompositionService bodyCompositionService;

  public BodyCompositionController(BodyCompositionService bodyCompositionService) {
    this.bodyCompositionService = bodyCompositionService;
  }

  @Operation(summary = "체성분 데이터 생성",
      description = "로그인한 사용자의 특정 날짜 체성분 데이터를 생성(저장)합니다. 이미 해당 날짜에 데이터가 있다면 덮어씁니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "데이터 생성 성공 (생성된 ID 반환)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패 (날짜 누락 등)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  @PostMapping("/info")
  public ResponseEntity<ApiResponse<Long>> createBodyComposition(
      @Valid @RequestBody BodyCompositionDto bodyCompositionDto,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    Long bodyCompositionId = bodyCompositionService.saveOrUpdateBodyComposition(bodyCompositionDto,
        userId);

    URI location = URI.create("/api/body/info/" + bodyCompositionId);

    return ResponseEntity.created(location)
        .body(ApiResponse.of(bodyCompositionId));
  }


  @Operation(summary = "체성분 데이터 기간 조회 (페이징)",
      description = "로그인한 사용자의 특정 기간(startDate ~ endDate) 체성분 데이터를 페이징하여 조회합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (페이징된 데이터 반환)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 날짜 형식 또는 기간"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  @GetMapping("/info")
  public ResponseEntity<ApiResponse<List<BodyCompositionResponse>>> getBodyCompositions(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      Pageable pageable) {

    Long userId = userPrincipal.getUserId();
    Page<BodyCompositionResponse> bodyInfoPage = bodyCompositionService.findByUserIdAndDateRange(
        userId, startDate, endDate, pageable);

    return ResponseEntity.ok(ApiResponse.of(bodyInfoPage));
  }

  @Operation(summary = "체성분 데이터 삭제",
      description = "특정 ID의 체성분 데이터를 삭제합니다. 본인 데이터만 삭제 가능합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없거나 삭제 권한이 없음")
  })
  @DeleteMapping("/info/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteBodyInfo(
      @PathVariable("id") Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    bodyCompositionService.deleteBodyInfo(id, userId);

    return ResponseEntity.ok(ApiResponse.empty());
  }


  @Operation(summary = "체성분 데이터 수정",
      description = "특정 ID의 체성분 데이터를 수정합니다. 본인 데이터만 수정 가능합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공 (수정된 ID 반환)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없거나 수정 권한이 없음")
  })
  @PutMapping("/info/{id}")
  public ResponseEntity<ApiResponse<Long>> updateBodyComposition(
      @PathVariable Long id,
      @RequestBody @Valid BodyCompositionDto dto,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    bodyCompositionService.updateBodyComposition(id, userId, dto);

    return ResponseEntity.ok(ApiResponse.of(id));
  }
}