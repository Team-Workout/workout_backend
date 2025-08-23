package com.workout.utils.controller;


import com.workout.auth.domain.UserPrincipal;
import com.workout.utils.dto.FileResponse;
import com.workout.utils.service.FileService;
import lombok.RequiredArgsConstructor;
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
   * 단일 파일 업로드
   */
  @PostMapping("/file")
  public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    FileResponse response = fileService.uploadFile(file, userId);

    return ResponseEntity.ok(response);
  }


}
