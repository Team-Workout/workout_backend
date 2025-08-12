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
      ResponseEntity<SigninResponse> response = restTemplate.postForEntity("/api/auth/signup",
          requestDto, SigninResponse.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().userId()).isNotNull();

      // DB에 실제로 저장되었는지 추가 검증
      User foundUser = userRepository.findByEmail("new@example.com").orElseThrow();
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
      // [개선] 응답 본문에 구체적인 에러 메시지가 포함되어 있는지 검증
      assertThat(response.getBody()).contains("존재하는 이메일 입니다");
    }

    @DisplayName("실패: 유효하지 않은 정보로 회원가입 시 400 Bad Request와 필드별 에러 메시지를 반환한다")
    @ParameterizedTest
    @MethodSource("com.workout.auth.controller.AuthControllerTest#invalidSignupRequests") // 정적 메소드 경로 명시
    void signup_failure_invalidRequest(SignupRequest invalidRequest, String expectedErrorMessage) {
      // when
      ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signup", invalidRequest, String.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      // [개선] 주석 해제 및 활성화: 각 유효성 검증 규칙에 맞는 에러 메시지를 확인
      assertThat(response.getBody()).contains(expectedErrorMessage);
    }
  }

  // ParameterizedTest를 위한 데이터 소스. 클래스 외부 또는 다른 클래스에 있을 경우 전체 경로를 명시해야 함.
  // 이 클래스 내에 있으므로 #메소드명 으로도 가능하나, 명확성을 위해 전체 경로 사용도 좋은 방법.
  private static Stream<Arguments> invalidSignupRequests() {
    Long validGymId = 1L;
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
    @DisplayName("성공: 올바른 정보로 로그인 시 200 OK, 사용자 ID, 세션 쿠키를 반환한다")
    void signin_success() {
      // given
      SigninRequest requestDto = new SigninRequest(EXISTING_USER_EMAIL, EXISTING_USER_PASSWORD);

      // when
      ResponseEntity<SigninResponse> response = restTemplate.postForEntity("/api/auth/signin", requestDto, SigninResponse.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().userId()).isEqualTo(existingUser.getId().toString());

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
      // [개선] 응답 본문에 구체적인 에러 메시지가 포함되어 있는지 검증
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
      // [개선] 응답 본문에 구체적인 에러 메시지가 포함되어 있는지 검증
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
      HttpEntity<String> requestEntity = new HttpEntity<>(null, logoutHeaders);

      // when: 획득한 세션으로 로그아웃 요청
      ResponseEntity<String> logoutResponse = restTemplate.postForEntity("/api/auth/signout", requestEntity, String.class);

      // then
      assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(logoutResponse.getBody()).isEqualTo("Logout Successful");

      String invalidatedCookieHeader = logoutResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
      assertThat(invalidatedCookieHeader).isNotNull()
          .contains("SESSION=;") // [개선] 쿠키 값이 비워졌는지 확인
          .contains("Max-Age=0"); // [개선] 쿠키가 즉시 만료되는지 확인
    }

    @Test
    @DisplayName("성공: 로그인하지 않은 상태에서 로그아웃 요청 시에도 200 OK를 반환한다")
    void signout_success_whenNotLoggedIn() {
      // given
      HttpEntity<String> requestEntity = new HttpEntity<>(null, new HttpHeaders());

      // when
      ResponseEntity<String> logoutResponse = restTemplate.postForEntity("/api/auth/signout", requestEntity, String.class);

      // then
      assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(logoutResponse.getBody()).isEqualTo("Logout Successful");
      // 세션이 없었으므로 Set-Cookie 헤더가 없을 수 있거나, 있더라도 만료된 쿠키여야 함
      String setCookieHeader = logoutResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
      if (setCookieHeader != null) {
        assertThat(setCookieHeader).contains("Max-Age=0");
      }
    }
  }
}