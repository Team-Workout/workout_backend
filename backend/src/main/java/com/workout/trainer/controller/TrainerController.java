package com.workout.trainer.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.body.dto.BodyCompositionResponse;
import com.workout.body.service.BodyCompositionService;
import com.workout.global.dto.ApiResponse;
import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.service.TrainerService;
import com.workout.utils.dto.FileResponse;
import com.workout.utils.service.FileService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

  private final TrainerService trainerService;
  private final FileService fileService;
  private final BodyCompositionService bodyCompositionService;

  public TrainerController(TrainerService trainerService,
      FileService fileService, BodyCompositionService bodyCompositionService) {
    this.trainerService = trainerService;
    this.fileService = fileService;
    this.bodyCompositionService = bodyCompositionService;
  }

  /**
   * 본인 프로필 생성 및 수정
   */
  @PutMapping("/profile")
  public ResponseEntity<Void> createOrUpdateProfile(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Valid @RequestBody ProfileCreateDto profileCreateDto) {

    Long currentTrainerId = userPrincipal.getUserId();
    trainerService.updateProfile(currentTrainerId, profileCreateDto);
    return ResponseEntity.ok().build();
  }

  /**
   * 특정 트레이너 프로필 조회
   */
  @GetMapping("/{trainerId}/profile")
  public ResponseEntity<ProfileResponseDto> getTrainerProfile(
      @PathVariable Long trainerId) {

    ProfileResponseDto profile = trainerService.getProfile(trainerId);
    return ResponseEntity.ok(profile);
  }

  /**
   * 프로필 삭제
   */
  @DeleteMapping("/profile")
  public ResponseEntity<Void> deleteProfile(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long currentTrainerId = userPrincipal.getUserId();
    trainerService.deleteProfile(currentTrainerId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 회원 몸 사진 조회
   */
  @GetMapping("/members/{memberId}/body-images")
  public ResponseEntity<ApiResponse<List<FileResponse>>> getMemberBodyImages(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Long memberId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      Pageable pageable) {

    Long trainerId = userPrincipal.getUserId();
    Page<FileResponse> bodyImagesPage = fileService.findMemberBodyImagesByTrainer(trainerId, memberId, startDate, endDate, pageable);

    return ResponseEntity.ok(ApiResponse.of(bodyImagesPage));
  }

  @GetMapping("/members/{memberId}/body-compositions")
  public ResponseEntity<ApiResponse<List<BodyCompositionResponse>>> getMemberBodyCompositions(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Long memberId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      Pageable pageable) {

    Long trainerId = userPrincipal.getUserId();
    Page<BodyCompositionResponse> bodyInfoPage = bodyCompositionService.findDataByTrainer(trainerId, memberId, startDate, endDate, pageable);

    return ResponseEntity.ok(ApiResponse.of(bodyInfoPage));
  }
}
