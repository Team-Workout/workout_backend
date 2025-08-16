package com.workout.auth.controller;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({EmbeddedRedisConfig.class})
@DisplayName("AuthController 통합 테스트")
class AuthControllerTest {

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
  private final String EXISTING_USER_EMAIL = "existing@example.com";
  private final String EXISTING_USER_PASSWORD = "password123";

  @BeforeEach
  void setUp() {
    // 테스트 실행 전 데이터베이스 초기화
    userRepository.deleteAll();
    gymRepository.deleteAll();

    // 테스트용 헬스장 데이터 생성
    testGym = gymRepository.save(Gym.builder()
        .name("테스트 헬스장")
        .address("서울")
        .phoneNumber("010-1234-5678")
        .build());

    // 테스트용 기존 사용자 데이터 생성
    existingUser = User.builder()
        .email(EXISTING_USER_EMAIL)
        .name("기존사용자")
        .password(passwordEncoder.encode(EXISTING_USER_PASSWORD))
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
    @DisplayName("성공: 새로운 사용자 정보로 회원가입에 성공하면 201 Created와 사용자 ID를 반환한다")
    void signup_success() {
      // given
      SignupRequest requestDto = new SignupRequest(
          testGym.getId(),
          "new@example.com",
          "newPass123!@", // 비밀번호 패턴 만족
          "새사용자",
          Gender.FEMALE,
          Role.USER
      );

      // when
      ResponseEntity<Long> response = restTemplate.postForEntity("/api/auth/signup", requestDto, Long.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      Long newUserId = response.getBody();
      assertThat(newUserId).isNotNull().isPositive();

      // DB에 실제로 저장되었는지, 반환된 ID로 직접 검증하여 테스트 신뢰도 확보
      User foundUser = userRepository.findById(newUserId).orElseThrow();
      assertThat(foundUser.getEmail()).isEqualTo("new@example.com");
      assertThat(foundUser.getName()).isEqualTo("새사용자");
    }

    @Test
    @DisplayName("실패: 이미 존재하는 이메일로 회원가입 시 400 Bad Request와 에러 메시지를 반환한다")
    void signup_failure_duplicateEmail() {
      // given
      SignupRequest requestDto = new SignupRequest(
          testGym.getId(),
          EXISTING_USER_EMAIL,
          "newPass123!@",
          "다른사용자",
          Gender.FEMALE,
          Role.USER
      );

      // when
      ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signup",
          requestDto, String.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).contains("존재하는 이메일 입니다");
    }

    @DisplayName("실패: 유효하지 않은 정보로 회원가입 시 400 Bad Request와 필드별 에러 메시지를 반환한다")
    @ParameterizedTest
    @MethodSource("invalidSignupRequests") // non-static 메소드 참조하도록 수정
    void signup_failure_invalidRequest(SignupRequest invalidRequest, String expectedErrorMessage) {
      // when
      ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signup", invalidRequest, String.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).contains(expectedErrorMessage);
    }
  }

  // ParameterizedTest를 위한 데이터 소스 (non-static으로 변경)
  private Stream<Arguments> invalidSignupRequests() {
    Long validGymId = testGym.getId(); // @BeforeEach에서 생성된 testGym의 ID를 동적으로 사용
    return Stream.of(
        Arguments.of(new SignupRequest(validGymId, "not-an-email", "pass123!@", "name", Gender.MALE, Role.USER), "올바른 이메일 형식이 아닙니다"),
        Arguments.of(new SignupRequest(validGymId, "valid@email.com", "short", "name", Gender.MALE, Role.USER), "비밀번호는 영문과 숫자를 포함하여 8자 이상이어야 합니다"),
        Arguments.of(new SignupRequest(null, "valid@email.com", "pass123!@", "name", Gender.MALE, Role.USER), "헬스장 ID는 필수입니다"),
        Arguments.of(new SignupRequest(validGymId, "valid@email.com", "pass123!@", "", Gender.MALE, Role.USER), "이름은 필수입니다")
    );
  }

  @Nested
  @DisplayName("로그인 API (/api/auth/signin) 테스트")
  class SigninTest {

    @Test
    @DisplayName("성공: 올바른 정보로 로그인 시 200 OK, 사용자 정보(id, name), 세션 쿠키를 반환한다")
    void signin_success() {
      // given
      SigninRequest requestDto = new SigninRequest(EXISTING_USER_EMAIL, EXISTING_USER_PASSWORD);

      // when
      ResponseEntity<SigninResponse> response = restTemplate.postForEntity("/api/auth/signin", requestDto, SigninResponse.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      SigninResponse responseBody = response.getBody();
      assertThat(responseBody).isNotNull();
      assertThat(responseBody.id()).isEqualTo(existingUser.getId());
      assertThat(responseBody.name()).isEqualTo(existingUser.getName());

      String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
      assertThat(setCookieHeader).isNotNull().contains("SESSION");
    }

    @Test
    @DisplayName("실패: 잘못된 비밀번호로 로그인 시 400 Bad Request와 에러 메시지를 반환한다")
    void signin_failure_wrongPassword() {
      // given
      SigninRequest requestDto = new SigninRequest(EXISTING_USER_EMAIL, "wrong-password");

      // when
      ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signin", requestDto, String.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).contains("비밀번호가 일치하지 않습니다");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 이메일로 로그인 시 400 Bad Request와 에러 메시지를 반환한다")
    void signin_failure_userNotFound() {
      // given
      SigninRequest requestDto = new SigninRequest("nonexistent@example.com", "any-password");

      // when
      ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signin", requestDto, String.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).contains("가입되지 않은 이메일입니다");
    }
  }

  @Nested
  @DisplayName("로그아웃 API (/api/auth/signout) 테스트")
  class SignoutTest {
    @Test
    @DisplayName("성공: 로그인 상태에서 로그아웃 요청 시 200 OK와 만료된 세션 쿠키를 반환한다")
    void signout_success_whenLoggedIn() {
      // given: 먼저 로그인하여 세션을 획득
      SigninRequest loginDto = new SigninRequest(EXISTING_USER_EMAIL, EXISTING_USER_PASSWORD);
      ResponseEntity<SigninResponse> loginResponse = restTemplate.postForEntity("/api/auth/signin", loginDto, SigninResponse.class);
      String sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
      assertThat(sessionCookie).isNotNull();

      HttpHeaders logoutHeaders = new HttpHeaders();
      logoutHeaders.add(HttpHeaders.COOKIE, sessionCookie);
      HttpEntity<Void> requestEntity = new HttpEntity<>(null, logoutHeaders);

      // when: 획득한 세션으로 로그아웃 요청 (POST 메소드 명시)
      ResponseEntity<String> logoutResponse = restTemplate.exchange("/api/auth/signout", HttpMethod.POST, requestEntity, String.class);

      // then
      assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(logoutResponse.getBody()).isEqualTo("Logout Successful");

      String invalidatedCookieHeader = logoutResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
      assertThat(invalidatedCookieHeader).isNotNull()
          .contains("SESSION=;")
          .contains("Max-Age=0");
    }

    @Test
    @DisplayName("성공: 로그인하지 않은 상태에서 로그아웃 요청 시에도 200 OK를 반환한다")
    void signout_success_whenNotLoggedIn() {
      // given
      HttpEntity<Void> requestEntity = new HttpEntity<>(null, new HttpHeaders());

      // when
      ResponseEntity<String> logoutResponse = restTemplate.exchange("/api/auth/signout", HttpMethod.POST, requestEntity, String.class);

      // then
      assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(logoutResponse.getBody()).isEqualTo("Logout Successful");
    }
  }
}