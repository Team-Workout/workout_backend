package com.workout.auth.dto;

import com.workout.member.domain.Member;

public record SigninResponse(
    Long id,
    String name,
    Long gymId
) {

  public static SigninResponse from(Member member) {
    return new SigninResponse(
        member.getId(),
        member.getName(),
        member.getGym().getId());
  }
}
