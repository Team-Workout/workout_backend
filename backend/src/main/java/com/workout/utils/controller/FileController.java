package com.workout.utils.controller;


import com.workout.auth.domain.UserPrincipal;
import com.workout.member.service.MemberService;
import com.workout.utils.dto.FileResponse;
import com.workout.utils.service.FileService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/common")
public class FileController {

  private final FileService fileService;
  private final MemberService memberService;

  public FileController(FileService fileService, MemberService memberService) {
    this.fileService = fileService;
    this.memberService = memberService;
  }

  /**
   * 복수 파일 업로드
   */
  @PostMapping("/members/me/profile-image")
  public ResponseEntity<FileResponse> uploadProfileImage(
      @RequestParam("image") MultipartFile image,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    Long userId = userPrincipal.getUserId();
    FileResponse response = fileService.uploadProfileImage(image, memberService.findById(userId));
    return ResponseEntity.ok(response);
  }

  @PostMapping("/members/me/body-images")
  public ResponseEntity<List<FileResponse>> uploadBodyImages(
      @RequestParam("images") MultipartFile[] images,
      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    Long userId = userPrincipal.getUserId();
    List<FileResponse> responses = fileService.uploadBodyImages(images, date, memberService.findById(userId));
    return ResponseEntity.ok(responses);
  }

  /**
   * 이미지 조회
   */
  @GetMapping("/members/me/body-images")
  public ResponseEntity<List<FileResponse>> getMyBodyImages(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    Long userId = userPrincipal.getUserId();
    List<FileResponse> bodyImages = fileService.findBodyImagesByRecordDate(memberService.findById(userId), startDate, endDate);
    return ResponseEntity.ok(bodyImages);
  }

  /**
   * 파일 삭제
   */
  @DeleteMapping("/file/{id}")
  public ResponseEntity<Void> deleteFile(
      @PathVariable("id") Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    fileService.deleteFileById(id, userId);
    return ResponseEntity.noContent().build();
  }
}
