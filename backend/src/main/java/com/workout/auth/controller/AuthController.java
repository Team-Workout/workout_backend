package com.workout.auth.controller;

import com.workout.auth.dto.SigninRequest;
import com.workout.auth.dto.SigninResponse;
import com.workout.auth.dto.SignupRequest;
import com.workout.auth.service.AuthService;
import com.workout.global.dto.ApiResponse;
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
  public ResponseEntity<ApiResponse<SigninResponse>> signin(
      @RequestBody @Valid SigninRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
  ) {
    Member member = authService.login(request.email(), request.password(), httpRequest,
        httpResponse);

    SigninResponse responseDto = SigninResponse.from(member);

    return ResponseEntity.ok(ApiResponse.of(responseDto));
  }

  @PostMapping("/signup/user")
  public ResponseEntity<ApiResponse<Long>> signupUser(@Valid @RequestBody SignupRequest signupRequest) {
    Long memberId = authService.signup(signupRequest, Role.MEMBER);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(memberId));
  }

  @PostMapping("/signup/trainer")
  public ResponseEntity<ApiResponse<Long>> signupTrainer(@Valid @RequestBody SignupRequest signupRequest) {
    Long trainerId = authService.signup(signupRequest, Role.TRAINER);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(trainerId));
  }
}
