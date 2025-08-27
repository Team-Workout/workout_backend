package com.workout.utils.service;

import com.workout.member.domain.Member;
import com.workout.member.repository.MemberRepository;
import com.workout.utils.domain.UserFile;
import com.workout.utils.dto.FileResponse;
import com.workout.utils.repository.FileRepository;
import jakarta.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Slf4j
@Service
public class FileService {

  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
  private final MemberRepository memberRepository;
  private final FileRepository fileRepository;
  @Value("${upload.local.dir}")
  private String uploadDir;

  private static String getExtension(String fileName) {
    return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
  }

  private static String convertFileName(String fileName) {
    String extension = getExtension(fileName);
    return UUID.randomUUID() + "." + extension;
  }

  // 복수 파일 업로드
  public List<FileResponse> uploadFiles(final MultipartFile[] files, Long userId) {
    Member member = memberRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid User"));

    List<UserFile> userFiles = new ArrayList<>();
    List<FileResponse> responses = new ArrayList<>();

    for (MultipartFile file : files) {
      validateFile(file);

      String originalName = file.getOriginalFilename();
      String storedName = convertFileName(originalName);
      String fullPath = getFullPath(storedName);

      UserFile userFile = UserFile.from(
          member,
          fullPath,
          file.getSize(),
          file.getContentType()
      );

      userFiles.add(userFile);
      responses.add(new FileResponse(originalName, storedName));
      storeFile(fullPath, file);
    }

    fileRepository.saveAll(userFiles);

    return responses;
  }

  // 파일 조회
  public Resource findFile(Long fileId) {
    UserFile userFile = fileRepository.findById(fileId)
        .orElseThrow(() -> new EntityNotFoundException("File Not Exists."));

    File file = new File(userFile.getFilePath());
    if (!file.exists()) {
      throw new EntityNotFoundException("서버에 파일이 존재하지 않습니다.");
    }

    return new FileSystemResource(file);

  }

  // 파일 검증
  private void validateFile(MultipartFile file) {
    String originalName = file.getOriginalFilename();
    if (originalName == null || !originalName.contains(".")) {
      throw new IllegalArgumentException("Invalid fileName");
    }

    String ext = getExtension(originalName);
    String contentType = file.getContentType();

    if (!isValidImage(ext, contentType)) {
      throw new IllegalArgumentException("Invalid file type");
    }

    if (!isValidSize(file.getSize())) {
      throw new IllegalArgumentException("File size exceeds limit");
    }
  }

  private boolean isValidSize(long fileSize) {
    return fileSize <= MAX_FILE_SIZE;
  }

  private boolean isValidImage(String ext, String contentType) {
    List<String> allowedExts = List.of("jpg", "jpeg", "png", "gif", "webp");
    List<String> allowedMimeTypes = List.of("image/jpeg", "image/png", "image/gif", "image/webp");

    return allowedExts.contains(ext) && allowedMimeTypes.contains(contentType);
  }

  public String getFullPath(String storedName) {
    return uploadDir + "/" + storedName;
  }

  public void storeFile(String fullPath, MultipartFile file) {
    File dest = new File(fullPath);

    if (!dest.getParentFile().exists()) {
      boolean created = dest.getParentFile().mkdirs();
      if (!created) {
        log.error("Failed to create directory: {}", dest.getParentFile().getAbsolutePath());
        throw new RuntimeException("Directory creation failed");
      }
    }

    try {
      file.transferTo(dest);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    log.info("File stored: {}", fullPath);

  }

  public void deleteFile(Long id, Long userId) {
    UserFile userFile = fileRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("삭제할 파일을 찾을 수 없습니다."));

    if (!userFile.getMember().getId().equals(userId)) {
      throw new SecurityException("파일을 삭제할 권한이 없습니다.");
    }

    try {

      // 1. 로컬 서버 파일 삭제
      File file = new File(userFile.getFilePath());
      if (file.exists()) {
        boolean deleted = file.delete();
        if (!deleted) {
          throw new IOException("파일 삭제 실패: " + file.getAbsolutePath());
        } else {
          log.info("File Delete SUCCESS : fileId={}, userId={}", id, userId);
        }
      } else {
        log.warn("File Not Exists: {}", file.getAbsolutePath());
      }

      // 2. DB에서 파일 메타데이터 삭제
      fileRepository.delete(userFile);

    } catch (Exception e) {
      log.error("File Delete Error : fileId={}, userId={}", id, userId, e);
      throw new RuntimeException("파일 삭제에 실패했습니다.");
    }

  }

}
