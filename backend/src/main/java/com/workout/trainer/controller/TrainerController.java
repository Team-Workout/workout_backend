package com.workout.trainer.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.body.dto.BodyCompositionResponse;
import com.workout.body.service.BodyCompositionService;
import com.workout.global.dto.ApiResponse;
import com.workout.pt.service.contract.PTContractService;
import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.service.TrainerProfileService;
import com.workout.trainer.service.TrainerService;
import com.workout.utils.dto.FileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "트레이너 (Trainer)", description = "트레이너 프로필 및 회원 관리 관련 API")
@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

  private final TrainerService trainerService;
  private final TrainerProfileService trainerProfileService;
  private final PTContractService ptContractService;
  private final BodyCompositionService bodyCompositionService;

  public TrainerController(TrainerService trainerService,
      TrainerProfileService trainerProfileService, PTContractService ptContractService,
      BodyCompositionService bodyCompositionService) {
    this.trainerService = trainerService;
    this.trainerProfileService = trainerProfileService;
    this.ptContractService = ptContractService;
    this.bodyCompositionService = bodyCompositionService;
  }

  @Operation(summary = "특정 체육관의 트레이너 목록 조회 (공개)",
      description = "특정 체육관(gymId)에 소속된 트레이너 프로필 목록을 페이징하여 조회합니다. (GymController의 API와 동일)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "트레이너 목록 조회 성공 (페이징된 데이터 반환)")
  })
  @GetMapping
  public ResponseEntity<ApiResponse<List<ProfileResponseDto>>> getTrainerProfilesByGym(
      @RequestParam Long gymId,
      @PageableDefault(size = 10, sort = "name") Pageable pageable) {

    Page<ProfileResponseDto> trainerProfilePage = trainerService.getTrainerProfilesByGym(gymId,
        pageable);

    return ResponseEntity.ok(ApiResponse.of(trainerProfilePage));
  }

  @Operation(summary = "트레이너 프로필 생성/수정 (본인)",
      description = "로그인한 트레이너 본인의 프로필(경력, 학력, 자격증, 소개, 전문분야)을 생성하거나 덮어쓰기 수정합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 업데이트 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않았거나 트레이너 계정이 아님")
  })
  @PutMapping("/profile")
  public ResponseEntity<ApiResponse<Void>> createOrUpdateProfile(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Valid @RequestBody ProfileCreateDto profileCreateDto) {

    Long currentTrainerId = userPrincipal.getUserId();

    trainerProfileService.updateProfile(currentTrainerId, profileCreateDto);

    return ResponseEntity.ok(ApiResponse.empty());
  }

  @Operation(summary = "특정 트레이너 프로필 상세 조회 (공개)",
      description = "trainerId에 해당하는 트레이너의 전체 프로필 정보(경력, 학력, 자격증, 전문분야 등)를 조회합니다.")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 ID의 트레이너를 찾을 수 없음")
  })
  @GetMapping("/{trainerId}/profile")
  public ResponseEntity<ApiResponse<ProfileResponseDto>> getTrainerProfile(
      @PathVariable Long trainerId) {

    ProfileResponseDto profile = trainerProfileService.getProfile(trainerId);

    return ResponseEntity.ok(ApiResponse.of(profile));
  }

  @Operation(summary = "트레이너 계정 탈퇴 (본인)",
      description = "로그인한 트레이너 본인의 계정을 삭제합니다. (주의: 연결된 모든 데이터가 삭제되며 복구 불가)",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계정 삭제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  @DeleteMapping("/profile")
  public ResponseEntity<ApiResponse<Void>> deleteProfile(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long currentTrainerId = userPrincipal.getUserId();

    trainerService.deleteTrainerAccount(currentTrainerId);

    return ResponseEntity.ok(ApiResponse.empty());
  }

  @Operation(summary = "[트레이너 전용] 담당 회원의 바디 이미지 조회",
      description = "트레이너가 본인의 담당 회원(memberId)의 바디 이미지 목록을 기간 조회합니다. (회원이 공개 설정을 허용한 경우)",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 바디 이미지 조회 성공 (페이징)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 트레이너"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "담당 회원이 아니거나 회원이 정보 접근을 비공개함"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
  })
  @GetMapping("/members/{memberId}/body-images")
  public ResponseEntity<ApiResponse<List<FileResponse>>> getMemberBodyImages(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Long memberId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      Pageable pageable) {

    Long trainerId = userPrincipal.getUserId();

    Page<FileResponse> bodyImagesPage = ptContractService.findMemberBodyImagesByTrainer(trainerId,
        memberId, startDate, endDate, pageable);

    return ResponseEntity.ok(ApiResponse.of(bodyImagesPage));
  }

  @Operation(summary = "[트레이너 전용] 담당 회원의 체성분 기록 조회",
      description = "트레이너가 본인의 담당 회원(memberId)의 체성분 기록을 기간 조회합니다. (회원이 공개 설정을 허용한 경우)",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 체성분 기록 조회 성공 (페이징)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 트레이너"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "담당 회원이 아니거나 회원이 정보 접근을 비공개함"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
  })
  @GetMapping("/members/{memberId}/body-compositions")
  public ResponseEntity<ApiResponse<List<BodyCompositionResponse>>> getMemberBodyCompositions(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Long memberId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      Pageable pageable) {

    Long trainerId = userPrincipal.getUserId();

    ptContractService.validateClientBodyDataAccess(trainerId, memberId);

    Page<BodyCompositionResponse> bodyInfoPage = bodyCompositionService.findByUserIdAndDateRange(
        memberId, startDate, endDate, pageable);

    return ResponseEntity.ok(ApiResponse.of(bodyInfoPage));
  }
}
