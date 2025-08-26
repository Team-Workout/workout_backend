package com.workout.member.dto;

import com.workout.member.domain.Member;

public record ProfileResponse(
    String name,
    String email,
    boolean isOpenWorkoutRecord,
    String GymName
) {
  public static ProfileResponse from(Member member) {
    return new ProfileResponse(
        member.getName(),
        member.getEmail(),
        member.getIsOpenWorkoutRecord(),
        member.getGym().getName()
    );
  }
}
