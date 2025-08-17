package com.workout.auth.controller;

import com.workout.auth.dto.SigninRequest;
import com.workout.auth.dto.SigninResponse;
import com.workout.auth.dto.SignupRequest;
import com.workout.global.config.EmbeddedRedisConfig;
import com.workout.gym.domain.Gym;
import com.workout.gym.repository.GymRepository;
import com.workout.trainer.domain.Trainer;
import com.workout.member.domain.AccountStatus;
import com.workout.member.domain.Gender;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({EmbeddedRedisConfig.class})
@DisplayName("AuthController 통합 테스트")
class AuthControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private GymRepository gymRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private Gym testGym;
  private Member testMember;
  private Trainer testTrainer;
  private final String MEMBER_EMAIL = "member@example.com";
  private final String TRAINER_EMAIL = "trainer@example.com";
  private final String PASSWORD = "password123";

  @BeforeEach
  void setUp() {
    testGym = gymRepository.save(Gym.builder().name("테스트 헬스장").build());

    testMember = memberRepository.save(Member.builder()
        .email(MEMBER_EMAIL)
        .name("테스트멤버")
        .password(passwordEncoder.encode(PASSWORD))
        .gym(testGym).gender(Gender.MALE).role(Role.MEMBER).accountStatus(AccountStatus.ACTIVE)
        .build());

    testTrainer = memberRepository.save(Trainer.builder()
        .email(TRAINER_EMAIL)
        .name("테스트트레이너")
        .password(passwordEncoder.encode(PASSWORD))
        .gym(testGym).gender(Gender.FEMALE).role(Role.TRAINER).accountStatus(AccountStatus.ACTIVE)
        .introduction("테스트 소개글")
        .build());
  }

  @AfterEach
  void tearDown() {
    memberRepository.deleteAllInBatch();
    gymRepository.deleteAllInBatch();
  }

  @Nested
  @DisplayName("회원가입 API 테스트")
  class SignupApiTest {

    @Test
    @DisplayName("성공: 새로운 일반 사용자(MEMBER) 회원가입 시 201 Created를 반환한다")
    void signupUser_success() {
      SignupRequest requestDto = new SignupRequest(testGym.getId(), "new.member@example.com", "newPass123", "새로운멤버", Gender.MALE, Role.MEMBER);

      ResponseEntity<Long> response = restTemplate.postForEntity("/api/auth/signup/user", requestDto, Long.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      Long newUserId = response.getBody();
      Member foundUser = memberRepository.findById(newUserId).orElseThrow();
      assertThat(foundUser.getEmail()).isEqualTo("new.member@example.com");
      assertThat(foundUser.getRole()).isEqualTo(Role.MEMBER);
      assertThat(foundUser).isNotInstanceOf(Trainer.class);
    }

    @Test
    @DisplayName("성공: 새로운 트레이너(TRAINER) 회원가입 시 201 Created를 반환한다")
    void signupTrainer_success() {
      SignupRequest requestDto = new SignupRequest(testGym.getId(), "new.trainer@example.com", "newPass123", "새로운트레이너", Gender.FEMALE, Role.TRAINER);

      ResponseEntity<Long> response = restTemplate.postForEntity("/api/auth/signup/trainer", requestDto, Long.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      Long newTrainerId = response.getBody();
      Member foundTrainer = memberRepository.findById(newTrainerId).orElseThrow();
      assertThat(foundTrainer.getEmail()).isEqualTo("new.trainer@example.com");
      assertThat(foundTrainer.getRole()).isEqualTo(Role.TRAINER);
      assertThat(foundTrainer).isInstanceOf(Trainer.class);
    }

    @Test
    @DisplayName("실패: 이미 존재하는 이메일로 트레이너 회원가입 시 400 Bad Request를 반환한다")
    void signup_failure_duplicateEmail() {
      SignupRequest requestDto = new SignupRequest(testGym.getId(), TRAINER_EMAIL, "newPass123", "다른트레이너", Gender.MALE, Role.TRAINER);

      ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signup/trainer", requestDto, String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).contains("존재하는 이메일입니다");
    }
  }

  @Nested
  @DisplayName("로그인 API (/api/auth/signin) 테스트")
  class SigninTest {

    @Test
    @DisplayName("성공: 일반 사용자로 로그인 시 200 OK와 세션 쿠키를 반환한다")
    void signin_member_success() {
      SigninRequest requestDto = new SigninRequest(MEMBER_EMAIL, PASSWORD);

      ResponseEntity<SigninResponse> response = restTemplate.postForEntity("/api/auth/signin", requestDto, SigninResponse.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().id()).isEqualTo(testMember.getId());
      assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).isNotNull().contains("SESSION");
    }

    @Test
    @DisplayName("성공: 트레이너로 로그인 시 200 OK와 세션 쿠키를 반환한다")
    void signin_trainer_success() {
      SigninRequest requestDto = new SigninRequest(TRAINER_EMAIL, PASSWORD);

      ResponseEntity<SigninResponse> response = restTemplate.postForEntity("/api/auth/signin", requestDto, SigninResponse.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().id()).isEqualTo(testTrainer.getId());
      assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).isNotNull().contains("SESSION");
    }

    @Test
    @DisplayName("실패: 잘못된 비밀번호로 로그인 시 400 Bad Request를 반환한다")
    void signin_failure_wrongPassword() {
      SigninRequest requestDto = new SigninRequest(MEMBER_EMAIL, "wrong-password");

      ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signin", requestDto, String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).contains("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
  }

  @Nested
  @DisplayName("로그아웃 API (/api/auth/signout) 테스트")
  class SignoutTest {
    @Test
    @DisplayName("성공: 로그인 상태에서 로그아웃 요청 시 200 OK와 만료된 세션 쿠키를 반환한다")
    void signout_success_whenLoggedIn() {
      SigninRequest loginDto = new SigninRequest(MEMBER_EMAIL, PASSWORD);
      ResponseEntity<SigninResponse> loginResponse = restTemplate.postForEntity("/api/auth/signin", loginDto, SigninResponse.class);
      String sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

      HttpHeaders logoutHeaders = new HttpHeaders();
      logoutHeaders.add(HttpHeaders.COOKIE, sessionCookie);
      HttpEntity<Void> requestEntity = new HttpEntity<>(null, logoutHeaders);

      ResponseEntity<String> logoutResponse = restTemplate.exchange("/api/auth/signout", HttpMethod.POST, requestEntity, String.class);

      assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      String invalidatedCookie = logoutResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
      assertThat(invalidatedCookie).isNotNull().contains("Max-Age=0");
    }
  }
}