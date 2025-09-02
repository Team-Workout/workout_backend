package com.workout.global.version.controller;

import com.workout.global.version.service.DataSyncService;
import com.workout.global.version.service.MasterDataVersionService;
import com.workout.global.version.domain.DataVersion;
import com.workout.global.version.domain.MasterDataCategory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class DataSyncController {

  private final MasterDataVersionService versionService;
  private final DataSyncService dataSyncService;

  @PostMapping("/check-version")
  public ResponseEntity<List<String>> checkDataVersion(@RequestBody DataVersion clientVersion) {
    List<String> staleCategories = versionService.getStaleCategories(clientVersion);
    return ResponseEntity.ok(staleCategories);
  }

  @GetMapping("/{category}")
  public ResponseEntity<?> syncData(@PathVariable MasterDataCategory category) {
    List<?> syncData = dataSyncService.getSyncDataFor(category);
    return ResponseEntity.ok(syncData);
  }
}