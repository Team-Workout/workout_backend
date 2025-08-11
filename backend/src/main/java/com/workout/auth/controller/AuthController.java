package com.workout.auth.controller;

import com.workout.auth.dto.SigninRequest;
import com.workout.auth.dto.SigninResponse;
import com.workout.auth.dto.SignupRequest;
import com.workout.auth.service.AuthService;
import com.workout.user.domain.User;
import com.workout.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/signin")
    public ResponseEntity<SigninResponse> signin(@RequestBody SigninRequest signinRequest,
        HttpServletRequest request,
        HttpServletResponse response) { // request, response 파라미터 추가

        User user = authService.login(signinRequest.email(), signinRequest.password(), request, response); // response 전달
        SigninResponse result = new SigninResponse(user.getId().toString());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/signup")
    public ResponseEntity<SigninResponse> signup(@RequestBody SignupRequest signupRequest) {
        User user = userService.registerUser(signupRequest);
        SigninResponse response = new SigninResponse(user.getId().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
