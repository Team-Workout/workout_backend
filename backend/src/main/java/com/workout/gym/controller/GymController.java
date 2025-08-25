package com.workout.gym.controller;

import com.workout.global.dto.ApiResponse;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.service.TrainerService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gyms")
public class GymController {
  private final TrainerService trainerService;

  public GymController(TrainerService trainerService) {
    this.trainerService = trainerService;
  }

  /**
   * 특정 체육관의 모든 트레이너 프로필 목록 조회
   */
  @GetMapping("/gyms/{gymId}/trainers")
  public ResponseEntity<ApiResponse<List<ProfileResponseDto>>> getTrainerProfilesByGym(
      @PathVariable Long gymId,
      @PageableDefault(size = 10, sort = "name") Pageable pageable) {

    Page<ProfileResponseDto> trainerProfilePage = trainerService.getTrainerProfilesByGym(gymId, pageable);

    return ResponseEntity.ok(ApiResponse.of(trainerProfilePage));
  }

}
