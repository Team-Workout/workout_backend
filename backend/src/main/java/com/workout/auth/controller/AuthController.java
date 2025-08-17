package com.workout.auth.controller;

import com.workout.auth.dto.SigninRequest;
import com.workout.auth.dto.SigninResponse;
import com.workout.auth.dto.SignupRequest;
import com.workout.auth.service.AuthService;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.service.TrainerService;
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
  private final MemberService memberService;
  private final TrainerService trainerService;

  public AuthController(AuthService authService, MemberService memberService,
      TrainerService trainerService) {
    this.authService = authService;
    this.memberService = memberService;
    this.trainerService = trainerService;
  }

  @PostMapping("/signin")
  public ResponseEntity<SigninResponse> signin(@Valid @RequestBody SigninRequest signinRequest,
      HttpServletRequest request,
      HttpServletResponse response) {

    Member member = authService.login(signinRequest.email(), signinRequest.password(), request,
        response);
    SigninResponse signinResponse = new SigninResponse(member.getId(), member.getName());
    return ResponseEntity.ok(signinResponse);
  }

  // --- 회원가입 엔드포인트 분리 ---
  @PostMapping("/signup/user")
  public ResponseEntity<Long> signupUser(@Valid @RequestBody SignupRequest signupRequest) {
    // 일반 유저 생성은 UserService가 담당
    Member member = memberService.registerUser(signupRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(member.getId());
  }

  @PostMapping("/signup/trainer")
  public ResponseEntity<Long> signupTrainer(@Valid @RequestBody SignupRequest signupRequest) {
    // 트레이너 생성은 TrainerService가 담당
    Trainer trainer = trainerService.registerTrainer(signupRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(trainer.getId());
  }
}
