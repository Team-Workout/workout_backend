package com.workout.gym.controller;

import com.workout.global.dto.ApiResponse;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "체육관 (Gym)", description = "체육관 및 소속 트레이너 조회 등 공개 API")
@RestController
@RequestMapping("/api/gyms")
public class GymController {
  private final TrainerService trainerService;

  public GymController(TrainerService trainerService) {
    this.trainerService = trainerService;
  }

  @Operation(summary = "특정 체육관 소속 트레이너 목록 조회 (공개)",
      description = "gymId에 해당하는 체육관에 소속된 모든 트레이너의 프로필 목록을 페이징하여 조회합니다.")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "트레이너 목록 조회 성공 (페이징된 데이터 반환)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 ID의 체육관을 찾을 수 없음")
  })
  @GetMapping("/{gymId}/trainers")
  public ResponseEntity<ApiResponse<List<ProfileResponseDto>>> getTrainerProfilesByGym(
      @PathVariable Long gymId,
      @PageableDefault(size = 10, sort = "name") Pageable pageable) {

    Page<ProfileResponseDto> trainerProfilePage = trainerService.getTrainerProfilesByGym(gymId, pageable);

    return ResponseEntity.ok(ApiResponse.of(trainerProfilePage));
  }

}
