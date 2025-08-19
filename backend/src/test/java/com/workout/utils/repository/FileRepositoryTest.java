package com.workout.utils.repository;

import com.workout.member.domain.Member;
import com.workout.utils.domain.File;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class FileRepositoryTest {

    @Autowired
    private FileRepository fileRepository;

    @Test
    @DisplayName("userId와 category로 파일 select")
    void testFindByUserIdAndCategory() {
        // given
        Member testUser;
        testUser = Member.builder().id(1L).name("테스트유저1").build();

        File file1 = new File();
        file1.setMember(testUser);
        file1.setCategory("image");
        file1.setFilePath("test1.png");

        File file2 = new File();
        file2.setMember(testUser);
        file2.setCategory("doc");
        file2.setFilePath("test2.docx");

        fileRepository.save(file1);
        fileRepository.save(file2);

        // when
        List<File> result = fileRepository.findByUserIdAndCategory(1L, "image");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFilePath()).isEqualTo("test1.png");
    }
}
