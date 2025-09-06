package com.workout.pt.controller.contract;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.dto.response.ClientListResponse.MemberResponse;
import com.workout.pt.service.contract.PTContractService;
import com.workout.trainer.service.TrainerService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainer/clients")
public class TrainerClientsController {

  private final PTContractService ptContractService;

  public TrainerClientsController(PTContractService ptContractService) {
    this.ptContractService = ptContractService;
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<List<MemberResponse>>> getMyClients(
      @AuthenticationPrincipal UserPrincipal trainerUser,
      Pageable pageable
  ) {
    Long trainerId = trainerUser.getUserId();
    Page<MemberResponse> clientsPage = ptContractService.findMyClients(trainerId, pageable);

    return ResponseEntity.ok(ApiResponse.of(clientsPage));
  }
}

