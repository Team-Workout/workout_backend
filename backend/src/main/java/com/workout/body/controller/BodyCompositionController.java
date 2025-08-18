package com.workout.body.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.body.domain.BodyComposition;
import com.workout.body.dto.BodyCompositionDto;
import com.workout.body.service.BodyCompositionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/body")
public class BodyCompositionController {

  private static final Logger logger = LoggerFactory.getLogger(BodyCompositionController.class);
  @Autowired
  BodyCompositionService bodyCompositionService;

  /**
   * 새 체성분 데이터 생성
   */
  @PostMapping("/newInfo")
  public ResponseEntity<Map<String, String>> createBodyComposition(
      @Valid @RequestBody BodyCompositionDto BodyCompositionDto,
      BindingResult bindingResult,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    if (bindingResult.hasErrors()) {
      Map<String, String> errors = new HashMap<>();
      bindingResult.getFieldErrors().forEach(error ->
          errors.put(error.getField(), error.getDefaultMessage())
      );
      return ResponseEntity.badRequest().body(errors);
    }

    Long userId = userPrincipal.getUserId();
    Long bodyCompositionId = bodyCompositionService.createBodyComposition(BodyCompositionDto,
        userId);

    logger.trace("create bodyComposition SUCCESS");
    return ResponseEntity.created(URI.create("/api/body-new/" + bodyCompositionId)).build();
  }


  /**
   * 단일 사용자의 모든 체성분 내역 조회
   */
  @GetMapping("/getInfo")
  public ResponseEntity<List<BodyComposition>> getBodyInfo(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();

    List<BodyComposition> bodyInfoList = bodyCompositionService.findByUserId(userId);

    return ResponseEntity.ok(bodyInfoList);
  }


  /**
   * 체성분 데이터 삭제
   */
  @DeleteMapping("/deleteInfo/{id}")
  public ResponseEntity<?> deleteBodyInfo(@PathVariable("id") Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();

    try {
      bodyCompositionService.deleteBodyInfo(id, userId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error deleting body info", e);

      return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "체성분 데이터 삭제에 실패했습니다.", "details", e.getMessage()));
    }
  }


  /**
   * 체성분 데이터 수정
   */
  @PutMapping("/updateInfo/{id}")
  public ResponseEntity<Void> updateBodyComposition(
      @PathVariable Long id,
      @RequestBody @Valid BodyCompositionDto dto) {
    bodyCompositionService.updateBodyComposition(id, dto);
    return ResponseEntity.noContent().build();
  }

}
