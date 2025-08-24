package com.workout.utils.controller;


import com.workout.auth.domain.UserPrincipal;
import com.workout.utils.domain.UserFile;
import com.workout.utils.dto.FileResponse;
import com.workout.utils.service.FileService;
import com.workout.workout.dto.routine.RoutineResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common")
public class FileController {

  private final FileService fileService;

  /**
   * 복수 파일 업로드
   */
  @PostMapping("/files")
  public ResponseEntity<List<FileResponse>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files,
                                                                @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    List<FileResponse> responses = fileService.uploadFiles(files, userId);

    return ResponseEntity.ok(responses);
  }

  /**
   * 파일 삭제
   */
  @DeleteMapping("/file/{id}")
  public ResponseEntity<Void> deleteFile(
      @PathVariable("id") Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    fileService.deleteFile(id, userId);
    return ResponseEntity.noContent().build();
  }


  /**
   * 이미지 조회
   */
  @GetMapping("/file/{id}")
  public ResponseEntity<Resource> getFile(@PathVariable Long fileId) {
    Resource resource = fileService.findFile(fileId);

    return ResponseEntity.ok().body(resource);
  }

}
