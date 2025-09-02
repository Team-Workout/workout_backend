package com.workout.global.version.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/exercises")
public class ExerciseAdminController {
  /*private final ExerciseAdminService exerciseAdminService;


  @PutMapping
  public ResponseEntity<Void> bulkUpdateExercises(
      @RequestBody @Valid ExerciseAdminDto.BulkUpdateRequest request
  ) {
    exerciseAdminService.bulkUpdateExercises(request);
    return ResponseEntity.ok().build();
  }*/
}