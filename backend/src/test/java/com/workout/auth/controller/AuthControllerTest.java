// AuthControllerTest.java

package com.workout.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.workout.auth.dto.SigninRequest;
import com.workout.auth.dto.SigninResponse;
import com.workout.auth.dto.SignupRequest;
import com.workout.global.config.EmbeddedRedisConfig;
import com.workout.gym.domain.Gym;
import com.workout.gym.repository.GymRepository;
import com.workout.user.domain.AccountStatus;
import com.workout.user.domain.Gender;
import com.workout.user.domain.Role;
import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({EmbeddedRedisConfig.class})
@DisplayName("AuthController 통합 테스트")
class AuthControllerIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private GymRepository gymRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private Gym testGym;
  private User existingUser;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    gymRepository.deleteAll();

    testGym = gymRepository.save(Gym.builder()
        .name("테스트 헬스장")
        .address("서울")
        .phoneNumber("010-1234-5678")
        .build());

    existingUser = User.builder()
        .email("existing@example.com")
        .name("기존사용자")
        .password(passwordEncoder.encode("password123"))
        .gym(testGym)
        .gender(Gender.MALE)
        .role(Role.USER)
        .accountStatus(AccountStatus.ACTIVE)
        .build();
    userRepository.save(existingUser);
  }

  @Nested
  @DisplayName("회원가입 API (/api/auth/signup) 테스트")
  class SignupTest {

    @Test
    @DisplayName("성공: 새로운 사용자 정보로 회원가입에 성공하면 201 Created를 반환한다")
    void signup_success() {
      // given
      SignupRequest requestDto = new SignupRequest(testGym.getId(), "new@example.com", "newPass123",
          "새사용자", Gender.FEMALE, "goal", Role.USER);

      // when
      ResponseEntity<SigninResponse> response = restTemplate.postForEntity("/api/auth/signup",
          requestDto, SigninResponse.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().userId()).isNotNull();

      User foundUser = userRepository.findByEmail("new@example.com").orElseThrow();
      assertThat(foundUser.getName()).isEqualTo("새사용자");
    }

    @Test
    @DisplayName("실패: 이미 존재하는 이메일로 회원가입 시 400 Bad Request를 반환한다")
    void signup_failure_duplicateEmail() {
      // given
      SignupRequest requestDto = new SignupRequest(testGym.getId(), "existing@example.com",
          "newPass123", "다른사용자", Gender.FEMALE, "goal", Role.USER);

      // when
      ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signup",
          requestDto, String.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // [개선] 여러 유효성 검사 실패 케이스를 한 번에 테스트하여 견고성 증명
    @DisplayName("실패: 유효하지 않은 정보로 회원가입 시 400 Bad Request를 반환한다")
    @ParameterizedTest
    @MethodSource("invalidSignupRequests")
    void signup_failure_invalidRequest(SignupRequest invalidRequest, String expectedErrorMessage) {
      // when
      ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signup",
          invalidRequest, String.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      // 필요하다면 에러 메시지 검증 추가
      // assertThat(response.getBody()).contains(expectedErrorMessage);
    }

    // [추가] 유효성 검사 테스트를 위한 데이터 소스
    private static Stream<Arguments> invalidSignupRequests() {
      return Stream.of(
          Arguments.of(
              new SignupRequest(1L, "not-an-email", "pass123", "name", Gender.MALE, "g", Role.USER),
              "잘못된 이메일 형식"),
          Arguments.of(
              new SignupRequest(1L, "valid@email.com", "", "name", Gender.MALE, "g", Role.USER),
              "비밀번호 공백"),
          Arguments.of(
              new SignupRequest(null, "valid@email.com", "pass123", "name", Gender.MALE, "g",
                  Role.USER), "헬스장 ID null"),
          Arguments.of(
              new SignupRequest(1L, "valid@email.com", "pass123", "", Gender.MALE, "g", Role.USER),
              "이름 공백")
      );
    }
  }

  @Nested
  @DisplayName("로그인 API (/api/auth/signin) 테스트")
  class SigninTest {

    @Test
    @DisplayName("성공: 올바른 정보로 로그인 시 200 OK와 사용자 ID를 반환하고, 세션 쿠키를 발급한다")
    void signin_success() {
      // given
      SigninRequest requestDto = new SigninRequest("existing@example.com", "password123");

      // when
      ResponseEntity<SigninResponse> response = restTemplate.postForEntity("/api/auth/signin",
          requestDto, SigninResponse.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().userId()).isEqualTo(existingUser.getId().toString());

      String setCookieHeader = response.getHeaders().getFirst("Set-Cookie");
      assertThat(setCookieHeader).isNotNull().contains("SESSION");
    }

    @Test
    @DisplayName("실패: 잘못된 비밀번호로 로그인 시 400 Bad Request를 반환한다")
    void signin_failure_wrongPassword() {
      // given
      SigninRequest requestDto = new SigninRequest("existing@example.com", "wrong-password");

      // when
      ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signin",
          requestDto, String.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // [추가] 존재하지 않는 사용자에 대한 로그인 실패 케이스
    @Test
    @DisplayName("실패: 존재하지 않는 이메일로 로그인 시 400 Bad Request를 반환한다")
    void signin_failure_userNotFound() {
      // given
      SigninRequest requestDto = new SigninRequest("nonexistent@example.com", "any-password");

      // when
      ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signin",
          requestDto, String.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("로그아웃 API (/api/auth/signout) 테스트")
  class SignoutTest {

    // 기존 로그아웃 테스트는 세션 획득 및 무효화 검증을 잘 하고 있으므로 유지합니다.
    // 추가적으로, 로그인하지 않은 상태에서의 로그아웃 요청도 정상 처리되는지 확인하면 좋습니다.
    @Test
    @DisplayName("성공: 로그인 상태에서 로그아웃 요청 시 200 OK를 반환하고, 세션을 무효화한다")
    void signout_success() {
      // given
      SigninRequest loginDto = new SigninRequest("existing@example.com", "password123");
      ResponseEntity<SigninResponse> loginResponse = restTemplate.postForEntity("/api/auth/signin",
          loginDto, SigninResponse.class);
      String sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
      assertThat(sessionCookie).isNotNull();

      HttpHeaders logoutHeaders = new HttpHeaders();
      logoutHeaders.add(HttpHeaders.COOKIE, sessionCookie);
      HttpEntity<String> requestEntity = new HttpEntity<>(null, logoutHeaders);

      // when
      ResponseEntity<String> logoutResponse = restTemplate.postForEntity("/api/auth/signout",
          requestEntity, String.class);

      // then
      assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(logoutResponse.getBody()).isEqualTo("Logout Successful");

      String invalidatedCookieHeader = logoutResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
      assertThat(invalidatedCookieHeader).isNotNull().contains("Max-Age=0");
    }

    // [추가] 로그인하지 않은 상태에서의 로그아웃 시도
    @Test
    @DisplayName("성공: 로그인하지 않은 상태에서 로그아웃 요청 시에도 200 OK를 반환한다")
    void signout_success_whenNotLoggedIn() {
      // given
      HttpEntity<String> requestEntity = new HttpEntity<>(null, new HttpHeaders());

      // when
      ResponseEntity<String> logoutResponse = restTemplate.postForEntity("/api/auth/signout",
          requestEntity, String.class);

      // then
      assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(logoutResponse.getBody()).isEqualTo("Logout Successful");
    }
  }
}