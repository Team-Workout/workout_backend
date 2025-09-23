package com.workout.auth.dto;

import java.io.Serializable;
import lombok.Getter;

@Getter
public class SocialSignupInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String name;
  private final String email;
  private final String provider; // 어느 소셜 로그인인지 구분 (e.g., "google")

  public SocialSignupInfo(String name, String email, String provider) {
    this.name = name;
    this.email = email;
    this.provider = provider;
  }
}