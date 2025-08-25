package com.workout.pt.controller.contract;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.dto.response.ContractResponse;
import com.workout.pt.service.contract.PTContractService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pt/contract")
public class PTContractController {

  private final PTContractService ptContractService;

  public PTContractController(PTContractService ptContractService) {
    this.ptContractService = ptContractService;
  }

  @DeleteMapping("/{contractId}")
  public ResponseEntity<Void> cancelContract(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable Long contractId) {
    Long userId = user.getUserId();
    ptContractService.cancelContract(userId, contractId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<List<ContractResponse>>> getMyContracts(
      @AuthenticationPrincipal UserPrincipal user,
      Pageable pageable
  ) {
    Long userId = user.getUserId();
    Page<ContractResponse> response = ptContractService.getMyContracts(userId, pageable);
    return ResponseEntity.ok(ApiResponse.of(response));
  }
}