package com.workout.trainer.controller;


import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.service.TrainerService;
import jakarta.validation.Valid;
import java.nio.file.attribute.UserPrincipal;
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

  //프로필 저장
  @PutMapping("/profile")
  public ResponseEntity<Void> createOrUpdateProfile(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Valid @RequestBody ProfileCreateDto profileCreateDto) {

    Long currentTrainerId = userPrincipal.getUserId();
    //todo
    //스프링 시큐리티 적용된 버전과 호한해야됨

    trainerService.createProfile(currentTrainerId, profileCreateDto);

    return ResponseEntity.ok().build();
  }
  //프로필 조회
  @GetMapping("/{trainerId}/profile")
  public ResponseEntity<ProfileResponseDto> getTrainerProfile(
      @PathVariable Long trainerId) {

    ProfileResponseDto profile = trainerService.getProfile(trainerId);
    return ResponseEntity.ok(profile);
  }
  @GetMapping("/gyms/{gymId}/trainers")
  public ResponseEntity<List<ProfileResponseDto>> getTrainerProfilesByGym(@PathVariable Long gymId) {
    List<ProfileResponseDto> trainerProfiles = trainerService.getTrainerProfilesByGym(gymId);
    return ResponseEntity.ok(trainerProfiles);
  }
}
