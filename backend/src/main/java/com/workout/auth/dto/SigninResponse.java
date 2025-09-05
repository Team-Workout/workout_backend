package com.workout.auth.dto;

import com.workout.member.domain.Member;
import com.workout.member.domain.Role;

public record SigninResponse(
    Long id,
    String name,
    Long gymId,
    Role role,
    String profileImageUrl
) {

  public static SigninResponse from(Member member, String profileImageUrl) {
    return new SigninResponse(
        member.getId(),
        member.getName(),
        member.getGym().getId(),
        member.getRole(),
        profileImageUrl
    );
  }
}
