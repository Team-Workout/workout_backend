package com.workout.auth.dto;

import com.workout.member.domain.Member;
import com.workout.member.domain.Role;

public record SigninResponse(
    Long id,
    String name,
    Long gymId,
    Role role
) {

  public static SigninResponse from(Member member) {
    return new SigninResponse(
        member.getId(),
        member.getName(),
        member.getGym().getId(),
        member.getRole());
  }
}
