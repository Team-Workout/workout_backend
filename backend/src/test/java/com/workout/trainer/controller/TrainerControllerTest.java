package com.workout.trainer.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.workout.auth.dto.SigninRequest;
import com.workout.auth.dto.SigninResponse;
import com.workout.global.config.EmbeddedRedisConfig;
import com.workout.gym.domain.Gym;
import com.workout.gym.repository.GymRepository;
import com.workout.member.domain.AccountStatus;
import com.workout.member.domain.Gender;
import com.workout.member.domain.Role;
import com.workout.trainer.domain.Award;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.repository.AwardRepository;
import com.workout.trainer.repository.TrainerRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({EmbeddedRedisConfig.class})
@DisplayName("TrainerController 통합 테스트")
class TrainerControllerTest {

  private final String TRAINER_EMAIL = "trainer@example.com";
  private final String PASSWORD = "password123";
  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private TrainerRepository trainerRepository;
  @Autowired
  private AwardRepository awardRepository;
  @Autowired
  private GymRepository gymRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  private Gym testGym;
  private Trainer testTrainer;
  private String authTokenCookie; // [개선] 인증 쿠키를 필드로 관리하여 중복 제거

  /**
   * [개선] 로그인 및 인증 쿠키를 얻는 헬퍼 메소드
   */
  private String getAuthCookie() {
    SigninRequest loginDto = new SigninRequest(TRAINER_EMAIL, PASSWORD);
    ResponseEntity<SigninResponse> loginResponse = restTemplate.postForEntity("/api/auth/signin",
        loginDto, SigninResponse.class);
    return loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
  }

  @BeforeEach
  void setUp() {
    testGym = gymRepository.save(Gym.builder().name("테스트 헬스장").build());
    testTrainer = Trainer.builder()
        .email(TRAINER_EMAIL)
        .name("테스트트레이너")
        .password(passwordEncoder.encode(PASSWORD))
        .gym(testGym)
        .gender(Gender.FEMALE)
        .role(Role.TRAINER)
        .accountStatus(AccountStatus.ACTIVE)
        .build();
    testTrainer = trainerRepository.save(testTrainer);

    // 모든 테스트 전에 로그인하여 인증 토큰 미리 획득
    this.authTokenCookie = getAuthCookie();
  }

  @AfterEach
  void tearDown() {
    awardRepository.deleteAllInBatch();
    trainerRepository.deleteAllInBatch();
    gymRepository.deleteAllInBatch();
  }

  @Nested
  @DisplayName("프로필 생성 및 수정 (PUT /api/trainers/profile) 테스트")
  class UpdateProfileTest {

    @Test
    @DisplayName("성공: 로그인 후, 유효한 정보로 프로필을 수정하고 다시 조회했을 때 변경사항이 반영된다")
    void updateProfile_Success_And_VerifyByGet() {
      // given: 프로필 수정 요청 DTO 생성
      ProfileCreateDto.AwardDto awardDto = new ProfileCreateDto.AwardDto(null, "전국체전 1위",
          LocalDate.now(), "서울");
      ProfileCreateDto requestDto = new ProfileCreateDto("수정된 소개글입니다.", List.of(awardDto),
          Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Set.of("재활"));

      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.COOKIE, authTokenCookie);
      HttpEntity<ProfileCreateDto> requestEntity = new HttpEntity<>(requestDto, headers);

      // when: 프로필 수정 요청
      ResponseEntity<Void> response = restTemplate.exchange("/api/trainers/profile", HttpMethod.PUT,
          requestEntity, Void.class);

      // then: 수정 성공 확인
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

      // [개선] then (Read after Write): 수정한 프로필을 다시 API로 조회하여 검증
      HttpEntity<Void> getRequestEntity = new HttpEntity<>(null, headers);
      ResponseEntity<ProfileResponseDto> getResponse = restTemplate.exchange(
          "/api/trainers/{trainerId}/profile", HttpMethod.GET, getRequestEntity,
          ProfileResponseDto.class, testTrainer.getId()
      );

      assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      ProfileResponseDto updatedProfile = getResponse.getBody();
      assertThat(updatedProfile).isNotNull();
      assertThat(updatedProfile.introduction()).isEqualTo("수정된 소개글입니다.");
      assertThat(updatedProfile.awards()).hasSize(1);
      assertThat(updatedProfile.awards().get(0).awardName()).isEqualTo("전국체전 1위");
    }

    @Test
    @DisplayName("실패: 로그인하지 않고 프로필 수정을 시도하면 401 Unauthorized를 반환한다")
    void updateProfile_Fail_Unauthorized() {
      ProfileCreateDto requestDto = new ProfileCreateDto("소개글", Collections.emptyList(),
          Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
          Collections.emptySet());
      HttpEntity<ProfileCreateDto> requestEntity = new HttpEntity<>(requestDto);
      ResponseEntity<Void> response = restTemplate.exchange("/api/trainers/profile", HttpMethod.PUT,
          requestEntity, Void.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
  }

  /**
   * [추가] 프로필 삭제 API 테스트
   */
  @Nested
  @DisplayName("프로필 삭제 (DELETE /api/trainers/profile) 테스트")
  class DeleteProfileTest {

    @Test
    @DisplayName("성공: 로그인한 트레이너가 본인 프로필을 삭제한다")
    void deleteProfile_Success() {
      // given
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.COOKIE, authTokenCookie);
      HttpEntity<Void> requestEntity = new HttpEntity<>(null, headers);

      // when
      ResponseEntity<Void> response = restTemplate.exchange("/api/trainers/profile",
          HttpMethod.DELETE, requestEntity, Void.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      assertThat(trainerRepository.existsById(testTrainer.getId())).isFalse();
    }

    @Test
    @DisplayName("실패: 로그인하지 않고 프로필 삭제를 시도하면 401 Unauthorized를 반환한다")
    void deleteProfile_Fail_Unauthorized() {
      // when
      ResponseEntity<Void> response = restTemplate.exchange("/api/trainers/profile",
          HttpMethod.DELETE, null, Void.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  @DisplayName("프로필 조회 API 테스트")
  class GetProfileTest {

    @Test
    @DisplayName("성공: 특정 트레이너의 프로필 정보를 조회한다")
    void getTrainerProfile_Success() {
      // given
      awardRepository.save(Award.builder().trainer(testTrainer).awardName("테스트 수상").build());
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.COOKIE, authTokenCookie);
      HttpEntity<Void> requestEntity = new HttpEntity<>(null, headers);

      // when
      ResponseEntity<ProfileResponseDto> response = restTemplate.exchange(
          "/api/trainers/{trainerId}/profile", HttpMethod.GET, requestEntity,
          ProfileResponseDto.class, testTrainer.getId()
      );

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      ProfileResponseDto responseBody = response.getBody();
      assertThat(responseBody.trainerId()).isEqualTo(testTrainer.getId());
      assertThat(responseBody.name()).isEqualTo(testTrainer.getName());
      assertThat(responseBody.awards()).hasSize(1);
      assertThat(responseBody.awards().get(0).awardName()).isEqualTo("테스트 수상");
    }

    // ... 다른 조회 테스트들도 위와 같이 헬퍼 메소드를 사용하도록 수정 가능 ...
  }
}