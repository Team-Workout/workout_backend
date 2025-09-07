package com.workout.auth.controller;

import com.workout.global.dto.ApiResponse; // 1. DTO 클래스를 import 합니다.
import io.swagger.v3.oas.annotations.responses.ApiResponses; //
import com.workout.auth.dto.SigninRequest;
import com.workout.auth.dto.SigninResponse;
import com.workout.auth.dto.SignupRequest;
import com.workout.auth.service.AuthService;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "인증 (Authentication)", description = "사용자 로그인, 로그아웃 및 회원가입 관련 API") //
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @Operation(summary = "세션 로그인", description = "이메일과 비밀번호로 로그인을 요청합니다. 성공 시 응답 본문과 함께 세션 쿠키(SESSION)를 발급합니다.") //
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"), //
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (아이디 또는 비밀번호 불일치)") //
  })
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

  @Operation(summary = "일반 회원 회원가입", description = "일반 사용자(MEMBER) 계정을 생성합니다.")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공 (TRAINER ID 반환)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패 (DTO 제약조건 위반)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 이메일 (Conflict)")
  })
  @PostMapping("/signup/user")
  public ResponseEntity<ApiResponse<Long>> signupUser(@Valid @RequestBody SignupRequest signupRequest) {
    Long memberId = authService.signup(signupRequest, Role.MEMBER);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(memberId));
  }

  @Operation(summary = "세션 로그아웃", description = "현재 세션을 무효화하고 세션 쿠키를 삭제합니다. (SecurityConfig에 설정된 핸들러가 동작)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공")
  })
  @PostMapping("/signup/trainer")
  public ResponseEntity<ApiResponse<Long>> signupTrainer(@Valid @RequestBody SignupRequest signupRequest) {
    Long trainerId = authService.signup(signupRequest, Role.TRAINER);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(trainerId));
  }
}
