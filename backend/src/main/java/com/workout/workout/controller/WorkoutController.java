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
import org.springframework.web.bind.annotation.DeleteMapping;
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


  /**
   * 운동일지 생성
   */
  @PostMapping
  public ResponseEntity<Void> createWorkoutLog(
      @Valid @RequestBody WorkoutLogCreateRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    // [수정] 서비스 메소드 호출을 단순화된 시그니처에 맞게 변경
    Long userId = userPrincipal.getUserId();
    Long workoutLogId = workoutLogService.createWorkoutLog(request, userId);

    return ResponseEntity.created(URI.create("/api/workout-logs/" + workoutLogId)).build();
  }

  /**
   * 운동일지 상세 조회
   */
  @GetMapping("/{id}")
  public ResponseEntity<WorkoutLogResponse> getWorkoutLog(@PathVariable("id") Long id) {
    WorkoutLogResponse response = workoutLogService.findWorkoutLogById(id);
    return ResponseEntity.ok(response);
  }

  /**
   * [신규] 운동일지 삭제
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteWorkoutLog(
      @PathVariable("id") Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    workoutLogService.deleteWorkoutLog(id, userId);
    return ResponseEntity.noContent().build();
  }


  @GetMapping("/exercises/version")
  public ResponseEntity<Integer> getExerciseVersion() {
    return ResponseEntity.ok(1);
  }

}