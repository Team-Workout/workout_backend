package com.workout.pt.controller.contract;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.dto.response.ClientListResponse.MemberResponse;
import com.workout.pt.service.contract.PTContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PT - 트레이너 회원 관리", description = "트레이너가 본인의 클라이언트(회원) 목록을 관리하는 API")
@RestController
@RequestMapping("/api/trainer/clients")
public class TrainerClientsController {

  private final PTContractService ptContractService;

  public TrainerClientsController(PTContractService ptContractService) {
    this.ptContractService = ptContractService;
  }

  @Operation(summary = "[트레이너] 내 클라이언트(회원) 목록 조회",
      description = "로그인한 트레이너 본인에게 등록된(활성 계약 상태인) PT 회원 목록을 페이징하여 조회합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 목록 조회 성공 (페이징)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않았거나 트레이너가 아님")
  })
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