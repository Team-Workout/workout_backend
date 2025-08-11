package com.workout.workout.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.workout.dto.WorkoutLogCreateRequest;
import com.workout.workout.dto.WorkoutLogResponse;
import com.workout.workout.service.WorkoutLogService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/workout-logs") // 경로를 복수형으로 변경
public class WorkoutController {

  private final WorkoutLogService workoutLogService;

  public WorkoutController(WorkoutLogService workoutLogService) {
    this.workoutLogService = workoutLogService;
  }

  @PostMapping
  public ResponseEntity<Void> createWorkoutLog(
      @Valid @RequestBody WorkoutLogCreateRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) { // @AuthenticationPrincipal 사용!

    // Spring Security가 인증되지 않은 접근을 막아주므로 null 체크가 거의 필요 없음
    Long userId = userPrincipal.getUserId();
    Long workoutLogId = workoutLogService.createWorkoutLog(request, userId);

    return ResponseEntity.created(URI.create("/api/workout-logs/" + workoutLogId)).build();
  }



  @GetMapping("/{id}")
  public ResponseEntity<WorkoutLogResponse> getWorkoutLog(@PathVariable("id") Long id) {
    WorkoutLogResponse response = workoutLogService.findWorkoutLogById(id);
    return ResponseEntity.ok(response);
  }
}