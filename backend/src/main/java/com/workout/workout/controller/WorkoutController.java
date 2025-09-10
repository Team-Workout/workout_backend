package com.workout.workout.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.dto.log.WorkoutLogCreateRequest;
import com.workout.workout.dto.log.WorkoutLogResponse;
import com.workout.workout.dto.routine.RoutineCreateRequest;
import com.workout.workout.dto.routine.RoutineResponse;
import com.workout.workout.service.RoutineService;
import com.workout.workout.service.WorkoutLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "운동일지 및 루틴 (Workout)", description = "일반 운동일지(Workout Log) 및 루틴(Routine) CRUD API")
@Slf4j
@RestController
@RequestMapping("/api/workout") // 경로를 복수형으로 변경
public class WorkoutController {

  private final WorkoutLogService workoutLogService;
  private final RoutineService routineService;

  public WorkoutController(WorkoutLogService workoutLogService, RoutineService routineService) {
    this.workoutLogService = workoutLogService;
    this.routineService = routineService;
  }

  /**
   * 운동일지 생성
   */
  @Operation(summary = "운동일지 생성 (일반)",
      description = "PT 세션과 관계없는 일반 운동일지를 생성합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      // 201 응답 시 ApiResponse<Long> 반환
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "운동일지 생성 성공 (생성된 ID 반환)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  @PostMapping("/logs")
  public ResponseEntity<ApiResponse<Long>> createWorkoutLog(
      @Valid @RequestBody WorkoutLogCreateRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    Long userId = userPrincipal.getUserId();
    WorkoutLog workoutLog = workoutLogService.createWorkoutLog(request, userId);
    Long logId = workoutLog.getId();

    return ResponseEntity.created(URI.create("/api/workout/logs/" + logId))
        .body(ApiResponse.of(logId));
  }

  /**
   * 운동일지 상세 조회
   */
  @Operation(summary = "운동일지 상세 조회",
      description = "로그인한 사용자가 본인의 특정 운동일지(id) 상세 내역을 조회합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (ApiResponse<WorkoutLogResponse> 반환)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 운동일지가 아님 (권한 없음)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "운동일지를 찾을 수 없음")
  })
  @GetMapping("/logs/{id}")
  public ResponseEntity<ApiResponse<WorkoutLogResponse>> getWorkoutLog(
      @Parameter(description = "조회할 운동일지 ID", required = true) @PathVariable("id") Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    Long userId = userPrincipal.getUserId();
    WorkoutLogResponse response = workoutLogService.findWorkoutLogById(id, userId);
    return ResponseEntity.ok(ApiResponse.of(response));
  }

  /**
   * 운동일지 삭제
   */
  @Operation(summary = "운동일지 삭제",
      description = "로그인한 사용자가 본인의 특정 운동일지를 삭제합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      // [수정] 204 No Content -> 200 OK
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공 (ApiResponse<Void> 반환)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 운동일지가 아님 (삭제 권한 없음)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "운동일지를 찾을 수 없음")
  })
  @DeleteMapping("/logs/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteWorkoutLog(
      @Parameter(description = "삭제할 운동일지 ID", required = true) @PathVariable("id") Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    workoutLogService.deleteWorkoutLog(id, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  /**
   * 루틴 생성
   */
  @Operation(summary = "운동 루틴 생성",
      description = "로그인한 사용자가 본인만의 운동 루틴을 생성합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "루틴 생성 성공 (ApiResponse<Long> 반환)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  @PostMapping("/routine")
  public ResponseEntity<ApiResponse<Long>> createRoutine(
      @Valid @RequestBody RoutineCreateRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    Long routineId = routineService.createRoutine(request, userId);

    return ResponseEntity.created(URI.create("/api/workout/routine/" + routineId)).build();
  }

  /**
   * 루틴 생성 (트레이너가 회원에게 루틴 생성)
   */
  @PostMapping("/routine")
  public ResponseEntity<Void> createRoutine(
      @RequestParam Long trainerId,
      @RequestParam Long memberId,
      @Valid @RequestBody RoutineCreateRequest request) {

    Long routineId = routineService.createRoutineForMember(request, trainerId, memberId);

    return ResponseEntity.created(URI.create("/api/workout/routine/" + routineId)).build();
  }

  /**
   * 루틴 조회
   */
  @Operation(summary = "특정 루틴 상세 조회 (공개)",
      description = "특정 루틴 ID에 해당하는 상세 정보(운동 목록, 세트 등)를 조회합니다. (인증 불필요)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (ApiResponse<RoutineResponse> 반환)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "루틴을 찾을 수 없음")
  })
  @GetMapping("/routine/{id}")
  public ResponseEntity<ApiResponse<RoutineResponse>> getRoutine(
      @Parameter(description = "조회할 루틴 ID", required = true) @PathVariable("id") Long id) {
    RoutineResponse response = routineService.findRoutineById(id);
    return ResponseEntity.ok(ApiResponse.of(response));
  }

  /**
   * 나의 루틴 조회
   */
  @Operation(summary = "나의 모든 루틴 조회",
      description = "로그인한 사용자가 생성한 모든 루틴 목록을 조회합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 루틴 목록 조회 성공 (ApiResponse<List<RoutineResponse>> 반환)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  @GetMapping("me/routines")
  public ResponseEntity<ApiResponse<List<RoutineResponse>>> getMyRoutine(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    Long userId = userPrincipal.getUserId();
    List<RoutineResponse> responses = routineService.findAllRoutinesByUserId(userId);
    return ResponseEntity.ok(ApiResponse.of(responses));
  }

  /**
   * 루틴 삭제
   */
  @Operation(summary = "루틴 삭제",
      description = "로그인한 사용자가 본인이 생성한 루틴을 삭제합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공 (ApiResponse<Void> 반환)"),
      // [수정] 204 -> 200
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 루틴이 아님 (삭제 권한 없음)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "루틴을 찾을 수 없음")
  })
  @DeleteMapping("/routine/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteRoutine(
      @Parameter(description = "삭제할 루틴 ID", required = true) @PathVariable("id") Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    routineService.deleteRoutine(id, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }

  /**
   * 운동 일지 조회
   */
  @Operation(summary = "나의 월별 운동일지 목록 조회",
      description = "로그인한 사용자의 특정 년/월(Year/Month)에 해당하는 모든 운동일지 목록을 조회합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월별 운동일지 조회 성공 (ApiResponse<List<WorkoutLogResponse>> 반환)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  @GetMapping("/me/logs/{year}/{month}")
  public ResponseEntity<ApiResponse<List<WorkoutLogResponse>>> getMyWorkoutLogsByMonth(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Parameter(description = "조회할 년도(YYYY)", example = "2025", required = true) @PathVariable("year") int year,
      @Parameter(description = "조회할 월(M 또는 MM)", example = "9", required = true) @PathVariable("month") int month) {
    Long userId = userPrincipal.getUserId();
    List<WorkoutLogResponse> responses = workoutLogService.findMyWorkoutLogsByMonth(userId, year,
        month); //
    return ResponseEntity.ok(ApiResponse.of(responses));
  }


  @Operation(summary = "운동 데이터 버전 조회 (임시)",
      description = "클라이언트 동기화용 정적 데이터 버전을 반환합니다. (추후 DataSyncController로 통합될 수 있음)",
      hidden = true) // DataSyncController로 통합될 예정이라면 숨김 처리
  @GetMapping("/workout-logs/exercises/version")
  public ResponseEntity<ApiResponse<Integer>> getExerciseVersion() {
    return ResponseEntity.ok(ApiResponse.of(1));
  }

}