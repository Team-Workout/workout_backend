package com.workout.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.workout.gym.domain.Gym;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("User 엔티티 단위 테스트")
class UserTest {

  private Gym testGym;

  @BeforeEach
  void setUp() {
    // 테스트에 필요한 Gym 객체 생성
    testGym = new Gym(1L, "테스트 헬스장", "주소", "번호");
  }

  @Nested
  @DisplayName("equals()와 hashCode() 계약 테스트")
  class EqualsAndHashCodeTest {

    @Test
    @DisplayName("모든 분기문을 포함하여 equals와 hashCode 계약을 검증한다")
    void equalsAndHashCode_Contract() {
      // given
      User user1 = User.builder().id(1L).name("사용자A").gym(testGym).build();
      User user2 = User.builder().id(1L).name("사용자B").gym(testGym).build(); // ID는 같고 내용은 다름
      User user3 = User.builder().id(2L).name("사용자A").gym(testGym).build(); // ID가 다름
      Object notUser = new Object();

      // 1. 자기 자신과의 비교
      assertThat(user1.equals(user1)).isTrue();

      // 2. ID가 같으면 내용은 달라도 true여야 한다.
      assertThat(user1.equals(user2)).isTrue();
      // ID가 같으므로 hashCode도 같아야 한다.
      assertThat(user1.hashCode()).isEqualTo(user2.hashCode());

      // 3. ID가 다르면 false여야 한다.
      assertThat(user1.equals(user3)).isFalse();

      // 4. null 또는 다른 타입과 비교
      assertThat(user1.equals(null)).isFalse();
      assertThat(user1.equals(notUser)).isFalse();
    }
  }

  @Nested
  @DisplayName("Getter 및 생성자 테스트")
  class GetterAndBuilderTest {

    @Test
    @DisplayName("Builder와 Getter가 올바르게 동작한다")
    void builder_and_getters_work_correctly() {
      // given
      String name = "테스트유저";
      String email = "test@example.com";

      // when
      User user = User.builder()
          .id(1L)
          .name(name)
          .email(email)
          .gym(testGym)
          .accountStatus(AccountStatus.ACTIVE)
          .gender(Gender.MALE)
          .role(Role.USER)
          .build();

      // then
      assertThat(user.getId()).isEqualTo(1L);

      assertThat(user.getName()).isEqualTo(name);
      assertThat(user.getEmail()).isEqualTo(email);
      assertThat(user.getGym()).isEqualTo(testGym);
      assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
      assertThat(user.getGender()).isEqualTo(Gender.MALE);
      assertThat(user.getRole()).isEqualTo(Role.USER);
    }
  }
}