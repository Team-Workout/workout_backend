package com.workout.pt.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.pt.dto.response.ClientListResponse;
import com.workout.pt.service.PTTrainerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainer/clients")
public class TrainerClientsController {

  private final PTTrainerService ptTrainerService;

  public TrainerClientsController(PTTrainerService ptTrainerService) {
    this.ptTrainerService = ptTrainerService;
  }

  /**
   * 관리 중인 pt 회원 조회
   */
  @GetMapping
  public ResponseEntity<ClientListResponse> getMyClients(
      @AuthenticationPrincipal UserPrincipal trainerPrincipal
  ) {
    return ResponseEntity.ok(ptTrainerService.findMyClients(trainerPrincipal));
  }
}

