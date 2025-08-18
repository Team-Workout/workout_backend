package com.workout.workout.controller;

import com.workout.workout.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/exercises")
@RequiredArgsConstructor
public class ExerciseAdminController {

  private final WorkoutService workoutService;

  @PatchMapping("/{exerciseId}/name")
  public ResponseEntity<Void> updateExerciseName(
      @PathVariable Long exerciseId,
      @RequestBody String newName) {

    workoutService.updateExerciseName(exerciseId, newName);
    return ResponseEntity.ok().build();
  }

  //버전 조회 함수

  //해당 버전의 데이터 다운
}