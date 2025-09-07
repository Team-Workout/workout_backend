package com.workout.global.version.controller;

import com.workout.global.version.service.DataSyncService;
import com.workout.global.version.service.MasterDataVersionService;
import com.workout.global.version.domain.DataVersion;
import com.workout.global.version.domain.MasterDataCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "데이터 동기화 (Data Sync)", description = "앱 마스터 데이터(운동, 근육) 버전 체크 및 동기화 API (공개)")
@RestController
@RequestMapping("/api/sync")
public class DataSyncController {

  private final MasterDataVersionService versionService;
  private final DataSyncService dataSyncService;

  public DataSyncController(MasterDataVersionService versionService, DataSyncService dataSyncService) {
    this.versionService = versionService;
    this.dataSyncService = dataSyncService;
  }

  @Operation(summary = "마스터 데이터 버전 체크",
      description = "클라이언트가 가진 데이터 버전과 서버의 최신 버전을 비교하여, 업데이트가 필요한 카테고리 목록(문자열 배열)을 반환합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "버전 체크 성공. (업데이트 필요한 카테고리 목록 반환. 빈 배열일 경우 최신 상태)")
  })
  @PostMapping("/check-version")
  public ResponseEntity<List<String>> checkDataVersion(@RequestBody DataVersion clientVersion) {
    List<String> staleCategories = versionService.getStaleCategories(clientVersion);
    return ResponseEntity.ok(staleCategories);
  }

  @Operation(summary = "마스터 데이터 동기화 (카테고리별)",
      description = "업데이트가 필요한 카테고리의 최신 마스터 데이터를 모두 반환합니다. (예: EXERCISE, MUSCLE)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "데이터 동기화 성공. (카테고리에 따라 ExerciseDto.SyncResponse 또는 MuscleDto.SyncResponse 등의 배열이 반환됩니다.)"),
      @ApiResponse(responseCode = "400", description = "잘못된 카테고리 이름")
  })
  @GetMapping("/{category}")
  public ResponseEntity<?> syncData(@PathVariable MasterDataCategory category) {
    List<?> syncData = dataSyncService.getSyncDataFor(category);
    return ResponseEntity.ok(syncData);
  }
}