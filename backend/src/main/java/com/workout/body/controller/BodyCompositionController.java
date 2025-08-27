// BodyCompositionController.java

package com.workout.body.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.body.dto.BodyCompositionDto;
import com.workout.body.dto.BodyCompositionResponse;
import com.workout.body.service.BodyCompositionService;
import com.workout.global.dto.ApiResponse;
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

@RestController
@RequestMapping("/api/body")
@RequiredArgsConstructor // Autowired 대신 생성자 주입 사용
public class BodyCompositionController {

  private final BodyCompositionService bodyCompositionService;

  /**
   * 새 체성분 데이터 생성
   */
  @PostMapping("/info")
  public ResponseEntity<Long> createBodyComposition(
      @Valid @RequestBody BodyCompositionDto bodyCompositionDto,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    Long bodyCompositionId = bodyCompositionService.saveOrUpdateBodyComposition(bodyCompositionDto,
        userId);

    return ResponseEntity.created(URI.create("/api/body/info/" + bodyCompositionId))
        .body(bodyCompositionId);
  }

  /**
   * 특정 기간 동안의 체성분 내역을 페이지네이션으로 조회
   */
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

  /**
   * 체성분 데이터 삭제
   */
  @DeleteMapping("/info/{id}")
  public ResponseEntity<Void> deleteBodyInfo(
      @PathVariable("id") Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    bodyCompositionService.deleteBodyInfo(id, userId);
    return ResponseEntity.ok().build();
  }

  /**
   * 체성분 데이터 수정
   */
  @PutMapping("/info/{id}")
  public ResponseEntity<Long> updateBodyComposition(
      @PathVariable Long id,
      @RequestBody @Valid BodyCompositionDto dto,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    bodyCompositionService.updateBodyComposition(id, userId, dto);
    return ResponseEntity.ok(id);
  }
}