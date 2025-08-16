package com.workout.trainer.controller;


import com.workout.auth.domain.UserPrincipal;
import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.service.TrainerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
  @PutMapping("/trainers/profile")
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
  @GetMapping("/trainers/{trainerId}/profile")
  public ResponseEntity<ProfileResponseDto> getTrainerProfile(
      @PathVariable Long trainerId) {

    ProfileResponseDto profile = trainerService.getProfile(trainerId);
    return ResponseEntity.ok(profile);
  }

  /**
   * 특정 체육관의 모든 트레이너 프로필 목록 조회
   */
  @GetMapping("/gyms/{gymId}/trainers")
  public ResponseEntity<List<ProfileResponseDto>> getTrainerProfilesByGym(@PathVariable Long gymId) {
    List<ProfileResponseDto> trainerProfiles = trainerService.getTrainerProfilesByGym(gymId);
    return ResponseEntity.ok(trainerProfiles);
  }
}
