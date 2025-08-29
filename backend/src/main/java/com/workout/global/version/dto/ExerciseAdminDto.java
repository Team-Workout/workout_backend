package com.workout.global.version.dto;

import com.workout.workout.domain.muscle.MuscleRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

// record는 그 자체로 final 클래스이며, 필드 getter, equals, hashCode, toString을 자동으로 생성해줍니다.
public record ExerciseAdminDto() { // 네임스페이스 역할을 위한 빈 record

  // Controller가 받을 최상위 요청 DTO
  public record BulkUpdateRequest(
      List<ExerciseUpdateItem> exercises
  ) {}

  // 개별 운동의 상태와 정보를 담는 DTO
  public record ExerciseUpdateItem(
      // id가 null이면 신규(CREATED), 있으면 기존(UPDATED, DELETED) 항목
      Long id,

      @NotBlank
      String name,

      // [핵심] 해당 운동에 연결될 타겟 근육들의 전체 목록
      List<TargetMuscleItem> targetMuscles,

      @NotNull
      UpdateStatus status
  ) {}

  // 개별 타겟 근육의 정보를 담는 DTO
  public record TargetMuscleItem(
      @NotNull
      Long muscleId,

      @NotNull
      MuscleRole muscleRole
  ) {}

  // 각 항목의 상태(생성, 수정, 삭제)를 나타내는 Enum
  public enum UpdateStatus {
    CREATED, UPDATED, DELETED
  }
}