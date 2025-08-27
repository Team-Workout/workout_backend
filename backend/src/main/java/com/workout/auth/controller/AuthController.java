package com.workout.auth.controller;

import com.workout.auth.dto.SigninRequest;
import com.workout.auth.dto.SigninResponse;
import com.workout.auth.dto.SignupRequest;
import com.workout.auth.service.AuthService;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/signin")
  public ResponseEntity<SigninResponse> signin(
      @Valid @RequestBody SigninRequest signinRequest,
      HttpServletRequest request,
      HttpServletResponse response) {

    //1 회원가입
    Member member = authService.login(signinRequest.email(), signinRequest.password(), request,
        response);
    SigninResponse signinResponse = SigninResponse.from(member);
    return ResponseEntity.ok(signinResponse);
  }

  @PostMapping("/signup/user")
  public ResponseEntity<Long> signupUser(@Valid @RequestBody SignupRequest signupRequest) {
    Long memberId = authService.signup(signupRequest, Role.MEMBER); // AuthService에 위임
    return ResponseEntity.status(HttpStatus.CREATED).body(memberId);
  }

  @PostMapping("/signup/trainer")
  public ResponseEntity<Long> signupTrainer(@Valid @RequestBody SignupRequest signupRequest) {
    Long trainerId = authService.signup(signupRequest, Role.TRAINER); // AuthService에 위임
    return ResponseEntity.status(HttpStatus.CREATED).body(trainerId);
  }
}
