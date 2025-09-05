package com.workout.auth.controller;

import com.workout.auth.dto.SigninRequest;
import com.workout.auth.dto.SigninResponse;
import com.workout.auth.dto.SignupRequest;
import com.workout.auth.service.AuthService;
import com.workout.global.dto.ApiResponse;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.service.MemberService;
import com.workout.utils.service.FileService;
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
    // 2. AuthService를 통해 로그인 처리 및 Member 엔티티 획득
    Member member = authService.login(request.email(), request.password(), httpRequest, httpResponse);

    // 3. FileService를 통해 프로필 이미지 URL 획득
    String profileImageUrl = member.getProfileImage().getStoredFileName();

    // 4. Member와 profileImageUrl을 함께 DTO로 변환
    SigninResponse responseDto = SigninResponse.from(member, profileImageUrl);

    return ResponseEntity.ok(ApiResponse.of(responseDto));
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
