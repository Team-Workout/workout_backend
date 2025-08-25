package com.workout.trainer.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.pt.service.contract.PTContractService;
import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.service.TrainerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

  private final TrainerService trainerService;

  public TrainerController(TrainerService trainerService) {
    this.trainerService = trainerService;
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
}
