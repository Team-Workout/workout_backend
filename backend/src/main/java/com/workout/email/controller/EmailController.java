package com.workout.email.controller;

import com.workout.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendVerificationEmail(@RequestBody String email) {
        emailService.sendEmail(email);
        return ResponseEntity.ok("이메일 전송 완료");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean result = emailService.verifyCode(email, code);
        return result ? ResponseEntity.ok("인증 성공") : ResponseEntity.status(400).body("인증 실패");
    }
}
