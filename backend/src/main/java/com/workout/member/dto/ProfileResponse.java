package com.workout.member.dto;

import com.workout.member.domain.Member;

public record ProfileResponse(
    String name,
    String email,
    boolean isOpenWorkoutRecord,
    boolean isOpenBodyImg,
    boolean IsOpenBodyComposition,
    String GymName
) {
  public static ProfileResponse from(Member member) {
    return new ProfileResponse(
        member.getName(),
        member.getEmail(),
        member.getIsOpenWorkoutRecord(),
        member.getIsOpenBodyImg(),
        member.getIsOpenBodyComposition(),
        member.getGym().getName()
    );
  }
}
