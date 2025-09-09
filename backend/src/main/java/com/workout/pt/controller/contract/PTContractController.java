package com.workout.pt.controller.contract;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.dto.response.ContractResponse;
import com.workout.pt.service.contract.PTContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "PT - 계약", description = "PT 계약 조회 및 취소 API")
@RestController
@RequestMapping("/api/pt/contract")
public class PTContractController {

  private final PTContractService ptContractService;

  public PTContractController(PTContractService ptContractService) {
    this.ptContractService = ptContractService;
  }

  @Operation(summary = "PT 계약 취소 (공통)",
      description = "로그인한 사용자(회원 또는 트레이너)가 본인과 관련된 PT 계약을 취소(종료)합니다. (계약 당사자만 가능)",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계약 취소(종료) 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인과 관련된 계약이 아님 (권한 없음)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "계약을 찾을 수 없음")
  })
  @DeleteMapping("/{contractId}")
  public ResponseEntity<ApiResponse<Void>> cancelContract(
      @AuthenticationPrincipal UserPrincipal user,
      @Parameter(description = "취소할 계약 ID", required = true) @PathVariable Long contractId) {
    Long userId = user.getUserId();
    ptContractService.cancelContract(userId, contractId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  @Operation(summary = "내 PT 계약 목록 조회 (공통)",
      description = "로그인한 사용자(회원 또는 트레이너) 본인과 관련된 모든 PT 계약 목록을 페이징하여 조회합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 계약 목록 조회 성공 (페이징)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
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