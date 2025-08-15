package com.workout.body.service;

import com.workout.body.domain.BodyComposition;
import com.workout.body.repository.BodyCompositionRepository;
import com.workout.gym.domain.Gym;
import com.workout.gym.repository.GymRepository;
import com.workout.user.domain.AccountStatus;
import com.workout.user.domain.Gender;
import com.workout.user.domain.Role;
import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class BodyCompositionServiceTest {

    @Autowired
    private BodyCompositionService bodyCompositionService;

    @Autowired
    private BodyCompositionRepository bodyCompositionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GymRepository gymRepository;

    @Test
    @DisplayName("deleteBodyInfo - ID와 UserID로 삭제 성공")
    void testDeleteBodyInfo_Success() {
        // given
        Gym gym = gymRepository.save(Gym.builder()
                .name("Test Gym")
                .address("Seoul")
                .build());

        User user = userRepository.save(User.builder()
                .gym(gym)
                .name("홍길동")
                .email("user@workout.com")
                .password("1234")
                .gender(Gender.MALE)
                .accountStatus(AccountStatus.ACTIVE)
                .role(Role.USER)
                .build());

        BodyComposition bodyComposition = bodyCompositionRepository.save(
                BodyComposition.builder()
                        .user(user)
                        .measurementDate(LocalDate.now())
                        .weightKg(70L)
                        .fatKg(20L)
                        .muscleMassKg(50L)
                        .build()
        );

        Long id = bodyComposition.getId();
        Long userId = user.getId();

        // when
        bodyCompositionService.deleteBodyInfo(id, userId);

        // then
        boolean exists = bodyCompositionRepository.findById(id).isPresent();
        assertThat(exists).isFalse();
    }
}
