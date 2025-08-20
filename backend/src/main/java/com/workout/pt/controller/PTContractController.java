package com.workout.pt.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.pt.dto.response.ContractResponse;
import com.workout.pt.service.PTContractService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    ptContractService.cancelContract(user, contractId);
    return ResponseEntity.noContent().build(); // 성공적으로 내용 없이 응답
  }

  @GetMapping("/me") // "/me"를 추가하여 '나의 계약'이라는 의미를 명확히 합니다.
  public ResponseEntity<List<ContractResponse>> getMyContracts(
      @AuthenticationPrincipal UserPrincipal user) {
    List<ContractResponse> contracts = ptContractService.getMyContracts(user);
    return ResponseEntity.ok(contracts);
  }
}