package com.workout.workout.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.workout.dto.log.WorkoutLogCreateRequest;
import com.workout.workout.dto.log.WorkoutLogResponse;
import com.workout.workout.dto.routine.RoutineCreateRequest;
import com.workout.workout.dto.routine.RoutineResponse;
import com.workout.workout.service.RoutineService;
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
@RequestMapping("/api/workout") // 경로를 복수형으로 변경
public class WorkoutController {

  private final WorkoutLogService workoutLogService;
  private final RoutineService routineService;

  public WorkoutController(WorkoutLogService workoutLogService, RoutineService routineService) {
    this.workoutLogService = workoutLogService;
    this.routineService = routineService;
  }

  /**
   * 운동일지 생성
   */
  @PostMapping("/logs")
  public ResponseEntity<Void> createWorkoutLog(
      @Valid @RequestBody WorkoutLogCreateRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    Long workoutLogId = workoutLogService.createWorkoutLog(request, userId);

    return ResponseEntity.created(URI.create("/api/workout-logs/" + workoutLogId)).build();
  }

  /**
   * 운동일지 상세 조회
   */
  @GetMapping("/logs/{id}")
  public ResponseEntity<WorkoutLogResponse> getWorkoutLog(@PathVariable("id") Long id) {
    WorkoutLogResponse response = workoutLogService.findWorkoutLogById(id);
    return ResponseEntity.ok(response);
  }

  /**
   * 운동일지 삭제
   */
  @DeleteMapping("/logs/{id}")
  public ResponseEntity<Void> deleteWorkoutLog(
      @PathVariable("id") Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    workoutLogService.deleteWorkoutLog(id, userId);
    return ResponseEntity.noContent().build();
  }


  /**
   * 루틴 생성
   */
  @PostMapping("/routine")
  public ResponseEntity<Void> createRoutine(
      @Valid @RequestBody RoutineCreateRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId(); //
    Long routineId = routineService.createRoutine(request, userId);

    return ResponseEntity.created(URI.create("/api/workout-logs/" + routineId)).build();
  }

  /**
   * 루틴 조회
   */
  @GetMapping("/routine/{id}")
  public ResponseEntity<RoutineResponse> getRoutine(@PathVariable("id") Long id) {
    RoutineResponse response = routineService.findRoutineById(id);
    return ResponseEntity.ok(response);
  }

  /**
   * 루틴 삭제
   */
  @DeleteMapping("/routine/{id}")
  public ResponseEntity<Void> deleteRoutine(
      @PathVariable("id") Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    routineService.deleteRoutine(id, userId);
    return ResponseEntity.noContent().build();
  }


  @GetMapping("/workout-logs/exercises/version")
  public ResponseEntity<Integer> getExerciseVersion() {
    return ResponseEntity.ok(1);
  }

}