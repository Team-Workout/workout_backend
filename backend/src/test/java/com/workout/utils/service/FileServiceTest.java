package com.workout.utils.service;

import com.workout.gym.domain.Gym;
import com.workout.gym.repository.GymRepository;
import com.workout.member.domain.AccountStatus;
import com.workout.member.domain.Gender;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import com.workout.utils.dto.FileResponse;
import com.workout.utils.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class FileServiceTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GymRepository gymRepository;

    @Autowired
    private FileRepository fileRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        Gym gym = gymRepository.save(Gym.builder()
            .name("Test Gym")
            .address("Seoul")
            .build());

        testMember = memberRepository.save(Member.builder()
            .gym(gym)
            .name("홍길동")
            .email("user@workout.com")
            .password("1234")
            .gender(Gender.MALE)
            .accountStatus(AccountStatus.ACTIVE)
            .role(Role.MEMBER)
            .build());
        memberRepository.save(testMember);
    }

    @Test
    void file_upload_success() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.png",
            "image/png",
            "dummy image content".getBytes()
        );

        // when
        FileResponse response = fileService.uploadFile(file, testMember.getId());

        // then
        assertThat(response.getOriginalFileName()).isEqualTo("test-image.png");
        assertThat(response.getStoredFileName()).isNotNull();

        // 실제 파일 저장 확인
        Path savedPath = Path.of(fileService.getFullPath(response.getStoredFileName()));
        assertThat(Files.exists(savedPath)).isTrue();

        // DB 저장 확인
        assertThat(fileRepository.findAll()).hasSize(1);
    }

    @Test
    void files_upload_sucess() throws IOException {
        // given
        MockMultipartFile file1 = new MockMultipartFile(
            "files",
            "test1.jpg",
            "image/jpeg",
            "image1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "files",
            "test2.png",
            "image/png",
            "image2".getBytes()
        );

        // when
        List<FileResponse> responses = fileService.uploadFiles(
            new MockMultipartFile[]{file1, file2}, testMember.getId()
        );

        // then
        assertThat(responses).hasSize(2);
        assertThat(fileRepository.findAll()).hasSize(2);

        for (FileResponse response : responses) {
            Path savedPath = Path.of(fileService.getFullPath(response.getStoredFileName()));
            assertThat(Files.exists(savedPath)).isTrue();
        }
    }
}
