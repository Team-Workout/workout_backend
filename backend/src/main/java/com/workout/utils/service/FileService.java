package com.workout.utils.service;

import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.FileErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import com.workout.pt.service.contract.PTTrainerService;
import com.workout.trainer.service.TrainerService;
import com.workout.utils.domain.ImagePurpose;
import com.workout.utils.domain.UserFile;
import com.workout.utils.dto.FileResponse;
import com.workout.utils.repository.FileRepository;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileService {

  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
  private final MemberService memberService;
  private final FileRepository fileRepository;
  private final TrainerService trainerService;
  private final PTTrainerService ptTrainerService;
  @Value("${upload.local.dir}")
  private String uploadDir;
  @Value("${default.profile.image.url}")
  private String defaultProfileImageUrl;

  public FileService(MemberService memberService, FileRepository fileRepository,
      TrainerService trainerService, PTTrainerService ptTrainerService) {
    this.memberService = memberService;
    this.fileRepository = fileRepository;
    this.trainerService = trainerService;
    this.ptTrainerService = ptTrainerService;
  }

  private static String getExtension(String fileName) {
    return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
  }

  private static String convertFileName(String fileName) {
    String extension = getExtension(fileName);
    return UUID.randomUUID() + "." + extension;
  }

  @PostConstruct
  public void init() {
    File directory = new File(uploadDir);
    if (!directory.exists()) {
      boolean created = directory.mkdirs();
      if (created) {
        log.info("업로드 디렉토리 생성 성공: {}", uploadDir);
      } else {
        log.error("업로드 디렉토리 생성 실패: {}", uploadDir);
      }
    }
  }

  @Transactional
  public List<FileResponse> uploadBodyImages(final MultipartFile[] files, final LocalDate dates,
      Long userId) {
    Member member = memberService.findById(userId);

    List<UserFile> userFilesToSave = new java.util.ArrayList<>();
    for (int i = 0; i < files.length; i++) {
      userFilesToSave.add(storeAndCreateUserFile(files[i], member, ImagePurpose.BODY, dates));
    }

    List<UserFile> savedFiles = fileRepository.saveAll(userFilesToSave);

    return savedFiles.stream()
        .map(FileResponse::from)
        .collect(Collectors.toList());
  }

  @Transactional
  public FileResponse uploadProfileImage(MultipartFile file, Long userId) {
    Member member = memberService.findById(userId);

    // 기존 프로필 이미지 처리
    UserFile oldProfileImage = member.getProfileImage();
    if (oldProfileImage != null) {
      fileRepository.delete(oldProfileImage);
      deletePhysicalFile(oldProfileImage.getStoredFileName());
    }

    UserFile newProfileImage = storeAndCreateUserFile(file, member, ImagePurpose.PROFILE, null);
    member.setProfileImage(newProfileImage);
    fileRepository.save(newProfileImage);
    return FileResponse.from(newProfileImage);
  }

  private UserFile storeAndCreateUserFile(MultipartFile file, Member member, ImagePurpose purpose,
      LocalDate recordDate) {
    validateFile(file);

    String originalName = file.getOriginalFilename();
    String storedName = convertFileName(originalName);
    String fullPath = getFullPath(storedName);

    storeFile(fullPath, file);

    return UserFile.from(
        member,
        storedName,
        originalName,
        file.getSize(),
        file.getContentType(),
        purpose,
        recordDate
    );
  }

  // 파일 조회
  public List<FileResponse> findBodyImagesByRecordDate(Long userId, LocalDate startDate,
      LocalDate endDate) {
    Member member = memberService.findById(userId);

    // 새로운 Repository 메소드 호출
    List<UserFile> userFiles = fileRepository.findByMemberIdAndPurposeAndRecordDateBetweenOrderByRecordDateDesc(
        member.getId(), ImagePurpose.BODY, startDate, endDate);

    return userFiles.stream()
        .map(FileResponse::from)
        .toList();
  }

  public String findProfile(Long userId) {
    Member member = memberService.findById(userId);
    return Optional.ofNullable(member.getProfileImage())
        .map(userFile -> "/images/" + userFile.getStoredFileName()) // 프로필 이미지가 있으면 해당 URL 생성
        .orElse(defaultProfileImageUrl);
  }

  public Page<FileResponse> findMemberBodyImagesByTrainer(Long trainerId, Long memberId,
      LocalDate startDate, LocalDate endDate, Pageable pageable) {
    log.info(trainerId + ", " + memberId + ", " + startDate + ", " + endDate + ", " + pageable);
    trainerService.findById(trainerId);

    if (!ptTrainerService.isMyClient(trainerId, memberId)) {
      throw new RestApiException(FileErrorCode.NOT_AUTHORITY);
    }

    Member member = memberService.findById(memberId);
    if (!member.getIsOpenWorkoutRecord()) {
      throw new RestApiException(FileErrorCode.NOT_AUTHORITY);
    }

    Page<UserFile> userFilesPage = fileRepository.findByMemberIdAndPurposeAndRecordDateBetweenOrderByRecordDateDesc(
        memberId, ImagePurpose.BODY, startDate, endDate, pageable);

    return userFilesPage.map(FileResponse::from);
  }

  // 파일 검증
  private void validateFile(MultipartFile file) {
    String originalName = file.getOriginalFilename();
    if (originalName == null || !originalName.contains(".")) {
      throw new RestApiException(FileErrorCode.INVALID_FILE_NAME);
    }

    String ext = getExtension(originalName);
    String contentType = file.getContentType();

    if (!isValidImage(ext, contentType)) {
      throw new RestApiException(FileErrorCode.INVALID_FILE_TYPE);
    }

    if (!isValidSize(file.getSize())) {
      throw new RestApiException(FileErrorCode.INVALID_FILE_SIZE);
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
    return uploadDir + File.separator + storedName;
  }

  public void storeFile(String fullPath, MultipartFile file) {
    File dest = new File(fullPath);

    if (!dest.getParentFile().exists()) {
      boolean created = dest.getParentFile().mkdirs();
      if (!created) {
        log.error("Failed to create directory: {}", dest.getParentFile().getAbsolutePath());
        throw new RestApiException(FileErrorCode.FILE_STORAGE_FAILED);
      }
    }

    try {
      file.transferTo(dest);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    log.info("File stored: {}", fullPath);

  }

  @Transactional
  public void deleteFileById(Long fileId, Long userId) {
    UserFile userFile = fileRepository.findById(fileId)
        .orElseThrow(() -> new RestApiException(FileErrorCode.FILE_NOT_FOUND));

    if (!userFile.getMember().getId().equals(userId)) {
      throw new RestApiException(FileErrorCode.NOT_AUTHORITY);
    }

    fileRepository.delete(userFile);
    deletePhysicalFile(userFile.getStoredFileName());
  }

  private void deletePhysicalFile(String storedFileName) {
    try {
      String fullPath = getFullPath(storedFileName);
      File file = new File(fullPath);
      if (file.exists()) {
        if (!file.delete()) {
          throw new RestApiException(FileErrorCode.FILE_DELETION_FAILED);
        } else {
          log.info("Physical file deleted: {}", fullPath);
        }
      } else {
        log.warn("Physical file not found for deletion: {}", fullPath);
      }
    } catch (Exception e) {
      log.error("Error deleting physical file: {}", storedFileName, e);
    }
  }
}
