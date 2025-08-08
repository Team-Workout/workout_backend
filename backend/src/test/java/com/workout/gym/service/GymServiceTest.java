package com.workout.gym.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.workout.gym.domain.Gym;
import com.workout.gym.repository.GymRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("GymService 단위 테스트")
class GymServiceTest {

  @Mock
  private GymRepository gymRepository;

  @InjectMocks
  private GymService gymService;

  @Nested
  @DisplayName("ID로 헬스장 존재 여부 확인 (existsById) 테스트")
  class ExistsByIdTest {

    @Test
    @DisplayName("성공: 헬스장이 존재하면 true를 반환한다")
    void existsById_returnsTrue_whenGymExists() {
      // given
      Long gymId = 1L;
      // gymRepository.existsById(1L)이 호출되면 true를 반환하도록 설정
      given(gymRepository.existsById(gymId)).willReturn(true);

      // when
      boolean result = gymService.existsById(gymId);

      // then
      assertThat(result).isTrue();
      // gymRepository.existsById가 1L을 인자로 정확히 1번 호출되었는지 검증
      then(gymRepository).should(times(1)).existsById(gymId);
    }

    @Test
    @DisplayName("성공: 헬스장이 존재하지 않으면 false를 반환한다")
    void existsById_returnsFalse_whenGymDoesNotExist() {
      // given
      Long gymId = 99L;
      // gymRepository.existsById(99L)이 호출되면 false를 반환하도록 설정
      given(gymRepository.existsById(gymId)).willReturn(false);

      // when
      boolean result = gymService.existsById(gymId);

      // then
      assertThat(result).isFalse();
      then(gymRepository).should(times(1)).existsById(gymId);
    }
  }

  @Nested
  @DisplayName("ID로 헬스장 조회 (findById) 테스트")
  class FindByIdTest {

    @Test
    @DisplayName("성공: 헬스장이 존재하면 해당 Gym 객체를 반환한다")
    void findById_returnsGym_whenGymExists() {
      // given
      Long gymId = 1L;
      // 테스트에 사용할 Gym 객체 생성
      Gym mockGym = Gym.builder()
          .id(gymId)
          .name("테스트 헬스장")
          .build();

      // gymRepository.findById(1L)이 호출되면 mockGym을 포함한 Optional을 반환하도록 설정
      given(gymRepository.findById(gymId)).willReturn(Optional.of(mockGym));

      // when
      Gym foundGym = gymService.findById(gymId);

      // then
      assertThat(foundGym).isNotNull();
      assertThat(foundGym.getId()).isEqualTo(gymId);
      assertThat(foundGym.getName()).isEqualTo("테스트 헬스장");
      then(gymRepository).should(times(1)).findById(gymId);
    }

    @Test
    @DisplayName("실패: 헬스장이 존재하지 않으면 IllegalArgumentException 예외를 던진다")
    void findById_throwsException_whenGymDoesNotExist() {
      // given
      Long gymId = 99L;
      // gymRepository.findById(99L)이 호출되면 빈 Optional을 반환하도록 설정
      given(gymRepository.findById(gymId)).willReturn(Optional.empty());

      // when & then
      // gymService.findById(99L)를 실행했을 때 IllegalArgumentException이 발생하는지 검증
      assertThrows(IllegalArgumentException.class, () -> {
        gymService.findById(gymId);
      });

      then(gymRepository).should(times(1)).findById(gymId);
    }
  }
}