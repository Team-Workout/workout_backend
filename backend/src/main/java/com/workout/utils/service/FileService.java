package com.workout.utils.service;

import com.workout.member.repository.MemberRepository;
import com.workout.utils.dto.FileResponse;
import com.workout.utils.domain.UserFile;
import com.workout.member.domain.Member;
import com.workout.utils.repository.FileRepository;

import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class FileService {

    @Value("${upload.local.dir}")
    private String uploadDir;

    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // 단일 파일 업로드
    public FileResponse uploadFile(MultipartFile file, Long userId){
        Member member = memberRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid User"));

        // 1. 파일 검사
        validateFile(file);

        // 2. DB에 파일의 메타데이터 저장
        String originalName = file.getOriginalFilename();
        String storedName = convertFileName(originalName);
        String fullPath = getFullPath(storedName);

        UserFile userFile = UserFile.from(
            member,
            storedName,
            fullPath,
            file.getSize(),
            file.getContentType()
        );

        fileRepository.save(userFile);

        // 3. 로컬에 파일 저장
        storeFile(fullPath, file);

        return new FileResponse(file.getOriginalFilename(), storedName);
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
                    storedName,
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

    private static String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private static String convertFileName(String fileName) {
        String extension = getExtension(fileName);
        return UUID.randomUUID() + "." + extension;
    }

    private boolean isValidImage(String ext, String contentType) {
        List<String> allowedExts = List.of("jpg", "jpeg", "png", "gif", "webp");
        List<String> allowedMimeTypes = List.of("image/jpeg", "image/png", "image/gif", "image/webp");

        return allowedExts.contains(ext) && allowedMimeTypes.contains(contentType);
    }

    public String getFullPath(String storedName) {
        return uploadDir + "/" + storedName;
    }

    public void storeFile(String fullPath, MultipartFile file){
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

}
