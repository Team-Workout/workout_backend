package com.workout.gym.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Gym 엔티티 단위 테스트")
class GymTest {

  @Nested
  @DisplayName("equals()와 hashCode() 계약 테스트")
  class EqualsAndHashCodeTest {

    @Test
    @DisplayName("모든 분기문을 포함하여 equals와 hashCode 계약을 검증한다")
    void equalsAndHashCode_Contract() {
      // given
      Gym gym1 = new Gym(1L, "헬스장A", "주소A", "번호A");
      Gym gym2 = new Gym(1L, "헬스장B", "주소B", "번호B"); // ID는 같고 내용은 다름
      Gym gym3 = new Gym(2L, "헬스장A", "주소A", "번호A"); // ID가 다름
      Object notGym = new Object();

      // 1. 자기 자신과의 비교 (if (this == o))
      assertThat(gym1.equals(gym1)).isTrue();

      // 2. ID가 같으면 내용은 달라도 true여야 한다.
      assertThat(gym1.equals(gym2)).isTrue();
      // ID가 같으므로 hashCode도 같아야 한다.
      assertThat(gym1.hashCode()).isEqualTo(gym2.hashCode());

      // 3. ID가 다르면 false여야 한다.
      assertThat(gym1.equals(gym3)).isFalse();
      // ID가 다르므로 hashCode도 다를 수 있다.
      assertThat(gym1.hashCode()).isNotEqualTo(gym3.hashCode());

      // 4. null과 비교 (if (o == null))
      assertThat(gym1.equals(null)).isFalse();

      // 5. 다른 타입의 객체와 비교 (if (thisClass != thatClass))
      assertThat(gym1.equals(notGym)).isFalse();
    }
  }

  @Nested
  @DisplayName("Getter 메서드 테스트")
  class GetterTest {

    @Test
    @DisplayName("Getter 메서드들이 올바른 값을 반환한다")
    void getters_ReturnCorrectValues() {
      // given
      Long id = 1L;
      String name = "테스트 헬스장";
      String address = "테스트 주소";
      String phoneNumber = "010-0000-0000";
      Gym gym = new Gym(id, name, address, phoneNumber);

      // when & then
      assertThat(gym.getId()).isEqualTo(id);
      assertThat(gym.getName()).isEqualTo(name);
      assertThat(gym.getAddress()).isEqualTo(address);
      assertThat(gym.getPhoneNumber()).isEqualTo(phoneNumber);
    }
  }

  @Nested
  @DisplayName("생성자 테스트")
  class ConstructorTest {

    @Test
    @DisplayName("AllArgsConstructor가 모든 필드를 올바르게 초기화한다")
    void allArgsConstructor_CreatesInstance() {
      // given & when
      Gym gym = new Gym(1L, "이름", "주소", "번호");

      // then
      assertThat(gym).isNotNull();
      assertThat(gym.getName()).isEqualTo("이름");
    }

    @Test
    @DisplayName("Builder가 객체를 올바르게 생성한다")
    void builder_CreatesInstance() {
      // given & when
      Gym gym = Gym.builder()
          .id(1L)
          .name("빌더 헬스장")
          .address("빌더 주소")
          .phoneNumber("빌더 번호")
          .build();

      // then
      assertThat(gym).isNotNull();
      assertThat(gym.getName()).isEqualTo("빌더 헬스장");
      assertThat(gym.getAddress()).isEqualTo("빌더 주소");
    }
  }
}