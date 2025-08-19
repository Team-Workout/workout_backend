package com.workout.utils.service;

import com.workout.utils.repository.FileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    public void findFiles(String fileCategory, Long memberId) {
        fileRepository.findByMemberIdAndCategory(memberId, fileCategory);
    }
}
