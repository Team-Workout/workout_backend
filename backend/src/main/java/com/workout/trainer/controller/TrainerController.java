package com.workout.trainer.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.body.dto.BodyCompositionResponse;
import com.workout.body.service.BodyCompositionService;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.service.contract.PTContractService;
import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.service.TrainerProfileService;
import com.workout.trainer.service.TrainerService;
import com.workout.utils.dto.FileResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
  private final TrainerProfileService trainerProfileService;
  private final PTContractService ptContractService;
  private final BodyCompositionService bodyCompositionService;

  public TrainerController(TrainerService trainerService,
      TrainerProfileService trainerProfileService, PTContractService ptContractService,
      BodyCompositionService bodyCompositionService) {
    this.trainerService = trainerService;
    this.trainerProfileService = trainerProfileService;
    this.ptContractService = ptContractService;
    this.bodyCompositionService = bodyCompositionService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<ProfileResponseDto>>> getTrainerProfilesByGym(
      @RequestParam Long gymId,
      @PageableDefault(size = 10, sort = "name") Pageable pageable) {

    Page<ProfileResponseDto> trainerProfilePage = trainerService.getTrainerProfilesByGym(gymId,
        pageable);

    return ResponseEntity.ok(ApiResponse.of(trainerProfilePage));
  }

  @PutMapping("/profile")
  public ResponseEntity<ApiResponse<Void>> createOrUpdateProfile(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Valid @RequestBody ProfileCreateDto profileCreateDto) {

    Long currentTrainerId = userPrincipal.getUserId();

    trainerProfileService.updateProfile(currentTrainerId, profileCreateDto);

    return ResponseEntity.ok(ApiResponse.empty());
  }

  @GetMapping("/{trainerId}/profile")
  public ResponseEntity<ApiResponse<ProfileResponseDto>> getTrainerProfile(
      @PathVariable Long trainerId) {

    ProfileResponseDto profile = trainerProfileService.getProfile(trainerId);

    return ResponseEntity.ok(ApiResponse.of(profile));
  }

  @DeleteMapping("/profile")
  public ResponseEntity<ApiResponse<Void>> deleteProfile(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long currentTrainerId = userPrincipal.getUserId();

    trainerService.deleteTrainerAccount(currentTrainerId);

    return ResponseEntity.ok(ApiResponse.empty());
  }

  @GetMapping("/members/{memberId}/body-images")
  public ResponseEntity<ApiResponse<List<FileResponse>>> getMemberBodyImages(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Long memberId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      Pageable pageable) {

    Long trainerId = userPrincipal.getUserId();

    Page<FileResponse> bodyImagesPage = ptContractService.findMemberBodyImagesByTrainer(trainerId,
        memberId, startDate, endDate, pageable);

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

    ptContractService.validateClientBodyDataAccess(trainerId, memberId);

    Page<BodyCompositionResponse> bodyInfoPage = bodyCompositionService.findByUserIdAndDateRange(
        memberId, startDate, endDate, pageable);

    return ResponseEntity.ok(ApiResponse.of(bodyInfoPage));
  }
}
