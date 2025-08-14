// UserServiceTest.java

package com.workout.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.workout.auth.dto.SignupRequest;
import com.workout.gym.domain.Gym;
import com.workout.gym.service.GymService;
import com.workout.global.Gender;
import com.workout.global.Role;
import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private GymService gymService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  // 'authenticate' 테스트는 이미 성공/실패 케이스가 잘 분리되어 있어 그대로 유지합니다.
  @Nested
  @DisplayName("사용자 인증 (authenticate) 테스트")
  class AuthenticateTests {

    // ... 기존 코드와 동일 ...
    @Test
    @DisplayName("성공: 올바른 이메일과 비밀번호로 인증에 성공한다")
    void authenticate_success() {
      // given
      String email = "test@example.com";
      String rawPassword = "password123";
      String encodedPassword = "encodedPassword";
      User mockUser = User.builder().password(encodedPassword).build();

      when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
      when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

      // when
      User authenticatedUser = userService.authenticate(email, rawPassword);

      // then
      assertThat(authenticatedUser).isEqualTo(mockUser);
      verify(userRepository, times(1)).findByEmail(email);
      verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 이메일로 인증을 시도한다")
    void authenticate_failure_userNotFound() {
      // given
      String email = "nonexistent@example.com";
      when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

      // when & then
      assertThrows(IllegalArgumentException.class,
          () -> userService.authenticate(email, "anyPassword"));
    }

    @Test
    @DisplayName("실패: 비밀번호가 일치하지 않아 인증에 실패한다")
    void authenticate_failure_passwordMismatch() {
      // given
      String email = "test@example.com";
      String rawPassword = "wrongPassword";
      String encodedPassword = "encodedPassword";
      User mockUser = User.builder().password(encodedPassword).build();

      when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
      when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

      // when & then
      assertThrows(IllegalArgumentException.class,
          () -> userService.authenticate(email, rawPassword));
    }
  }

  // 'registerUser' 메소드 관련 테스트 그룹
  @Nested
  @DisplayName("사용자 등록 (registerUser) 테스트")
  class RegisterUserTests {

    @Test
    @DisplayName("성공: 새로운 사용자를 성공적으로 등록한다")
    void registerUser_success() {
      // given
      SignupRequest request = new SignupRequest(1L, "new@example.com", "password123", "newUser",
          Gender.MALE, "goal", Role.USER);
      Gym mockGym = Gym.builder().id(1L).name("Test Gym").build();
      String encodedPassword = "encodedPassword";

      // [수정] findById 한번만 호출되도록 Mockito 설정 변경
      when(gymService.findById(request.gymId())).thenReturn(mockGym);
      when(userRepository.existsByName(request.name())).thenReturn(false);
      when(userRepository.existsByEmail(request.email())).thenReturn(false);
      when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);
      when(userRepository.save(any(User.class))).thenAnswer(
          invocation -> invocation.getArgument(0));

      // when
      userService.registerUser(request);

      // then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      User savedUser = userCaptor.getValue();

      assertThat(savedUser.getEmail()).isEqualTo(request.email());
      assertThat(savedUser.getGym()).isEqualTo(mockGym);
    }


    @Test
    @DisplayName("실패: 이미 존재하는 이메일로 등록을 시도한다")
    void registerUser_failure_duplicateEmail() {
      // given
      SignupRequest request = new SignupRequest(1L, "existing@example.com", "password123", "newUser", Gender.MALE, "goal", Role.USER);
      Gym mockGym = Gym.builder().id(1L).name("Test Gym").build();

      // [수정] findById를 통과해야 다음 단계로 갈 수 있으므로 설정
      when(gymService.findById(request.gymId())).thenReturn(mockGym);
      when(userRepository.existsByName(request.name())).thenReturn(false);
      when(userRepository.existsByEmail(request.email())).thenReturn(true);

      // when & then
      assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("실패: 이미 존재하는 이름으로 등록을 시도한다")
    void registerUser_failure_duplicateName() {
      // given
      SignupRequest request = new SignupRequest(1L, "new@example.com", "password123", "existingUser", Gender.MALE, "goal", Role.USER);
      Gym mockGym = Gym.builder().id(1L).name("Test Gym").build();

      // [수정] findById를 통과해야 다음 단계로 갈 수 있으므로 설정
      when(gymService.findById(request.gymId())).thenReturn(mockGym);
      when(userRepository.existsByName(request.name())).thenReturn(true);

      // when & then
      assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));

      verify(userRepository, never()).existsByEmail(anyString());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 Gym ID로 등록을 시도한다")
    void registerUser_failure_invalidGymId() {
      // given
      SignupRequest request = new SignupRequest(999L, "new@example.com", "password123", "newUser", Gender.MALE, "goal", Role.USER);

      // [수정] findById 호출 시 예외가 발생하는 상황을 설정
      when(gymService.findById(request.gymId()))
          .thenThrow(new IllegalArgumentException("존재하지 않는 헬스장입니다."));

      // when & then
      assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));

      // [수정] findById가 호출되었는지 확인
      verify(gymService, times(1)).findById(request.gymId());
      // findById에서 실패했으므로, 그 이후 로직은 호출되면 안 됨을 검증
      verify(userRepository, never()).existsByName(anyString());
      verify(userRepository, never()).save(any(User.class));
    }
  }
}
