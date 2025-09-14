package com.workout.auth.dto;

public record SigninResponse(String accessToken) {
  public static SigninResponse from(String accessToken) {
    return new SigninResponse(accessToken);
  }
}
