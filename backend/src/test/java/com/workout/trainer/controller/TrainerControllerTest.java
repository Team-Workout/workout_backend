package com.workout.trainer.controller;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({EmbeddedRedisConfig.class})
@DisplayName("TrainerController 통합 테스트")
class TrainerControllerTest {

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
  private final String TRAINER_EMAIL = "trainer@example.com";
  private final String PASSWORD = "password123";

  @BeforeEach
  void setUp() {
    testGym = gymRepository.save(Gym.builder().name("테스트 헬스장").build());

    testTrainer = trainerRepository.save(Trainer.builder()
        .email(TRAINER_EMAIL)
        .name("테스트트레이너")
        .password(passwordEncoder.encode(PASSWORD))
        .gym(testGym)
        .gender(Gender.FEMALE)
        .role(Role.TRAINER)
        .accountStatus(AccountStatus.ACTIVE)
        .build());
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
    @DisplayName("성공: 로그인 후, 유효한 정보로 프로필을 수정하면 DB에 반영된다")
    void updateProfile_Success() {
      // 1. 로그인하여 세션 쿠키 획득
      SigninRequest loginDto = new SigninRequest(TRAINER_EMAIL, PASSWORD);
      ResponseEntity<SigninResponse> loginResponse = restTemplate.postForEntity("/api/auth/signin", loginDto, SigninResponse.class);
      String sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

      // 2. 프로필 수정 요청 DTO 생성
      ProfileCreateDto.AwardDto awardDto = new ProfileCreateDto.AwardDto("전국체전 1위", LocalDate.now(), "서울");
      ProfileCreateDto requestDto = new ProfileCreateDto("수정된 소개글입니다.", List.of(awardDto), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Set.of("재활"));

      // 3. 획득한 쿠키를 헤더에 담아 프로필 수정 요청
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.COOKIE, sessionCookie);
      HttpEntity<ProfileCreateDto> requestEntity = new HttpEntity<>(requestDto, headers);
      ResponseEntity<Void> response = restTemplate.exchange("/api/trainers/profile", HttpMethod.PUT, requestEntity, Void.class);

      // 4. 검증
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

      Trainer updatedTrainer = trainerRepository.findById(testTrainer.getId()).orElseThrow();
      assertThat(updatedTrainer.getIntroduction()).isEqualTo("수정된 소개글입니다.");
      List<Award> awards = awardRepository.findAllByTrainerId(testTrainer.getId());
      assertThat(awards).hasSize(1);
      assertThat(awards.get(0).getAwardName()).isEqualTo("전국체전 1위");
    }

    @Test
    @DisplayName("실패: 로그인하지 않고 프로필 수정을 시도하면 401 Unauthorized를 반환한다")
    void updateProfile_Fail_Unauthorized() {
      ProfileCreateDto requestDto = new ProfileCreateDto("소개글", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptySet());
      HttpEntity<ProfileCreateDto> requestEntity = new HttpEntity<>(requestDto);
      ResponseEntity<Void> response = restTemplate.exchange("/api/trainers/profile", HttpMethod.PUT, requestEntity, Void.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  @DisplayName("프로필 조회 API 테스트")
  class GetProfileTest {

    @Test
    @DisplayName("성공: 특정 트레이너의 프로필 정보를 조회한다")
    void getTrainerProfile_Success() {
      // given: 조회할 프로필 상세 정보(수상내역) 추가
      awardRepository.save(Award.builder().trainer(testTrainer).awardName("테스트 수상").build());

      // --- 변경점 시작 ---
      // 1. 로그인
      SigninRequest loginDto = new SigninRequest(TRAINER_EMAIL, PASSWORD);
      ResponseEntity<SigninResponse> loginResponse = restTemplate.postForEntity("/api/auth/signin", loginDto, SigninResponse.class);
      String sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

      // 2. 헤더에 쿠키 추가
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.COOKIE, sessionCookie);
      HttpEntity<Void> requestEntity = new HttpEntity<>(null, headers);
      // --- 변경점 종료 ---

      // when
      // exchange를 사용하여 헤더를 포함한 요청 전송
      ResponseEntity<ProfileResponseDto> response = restTemplate.exchange(
          "/api/trainers/{trainerId}/profile",
          HttpMethod.GET,
          requestEntity,
          ProfileResponseDto.class,
          testTrainer.getId());

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      ProfileResponseDto responseBody = response.getBody();
      assertThat(responseBody.trainerId()).isEqualTo(testTrainer.getId());
      assertThat(responseBody.name()).isEqualTo(testTrainer.getName());
      assertThat(responseBody.awards()).hasSize(1);
      assertThat(responseBody.awards().get(0).awardName()).isEqualTo("테스트 수상");
    }

    @Test
    @DisplayName("성공: 특정 헬스장의 모든 트레이너 프로필 목록을 조회한다")
    void getTrainerProfilesByGym_Success() {
      // given: 같은 헬스장에 다른 트레이너 추가
      Trainer anotherTrainer = trainerRepository.save(Trainer.builder().email("another@test.com").name("다른트레이너").password("123").gym(testGym).role(Role.TRAINER).gender(Gender.MALE).accountStatus(AccountStatus.ACTIVE).build());

      // --- 변경점 시작 ---
      // 1. 로그인
      SigninRequest loginDto = new SigninRequest(TRAINER_EMAIL, PASSWORD);
      ResponseEntity<SigninResponse> loginResponse = restTemplate.postForEntity("/api/auth/signin", loginDto, SigninResponse.class);
      String sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

      // 2. 헤더에 쿠키 추가
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.COOKIE, sessionCookie);
      HttpEntity<Void> requestEntity = new HttpEntity<>(null, headers);
      // --- 변경점 종료 ---

      // when
      ResponseEntity<List<ProfileResponseDto>> response = restTemplate.exchange(
          "/api/trainers/gyms/{gymId}/trainers",
          HttpMethod.GET,
          requestEntity, // 헤더가 포함된 requestEntity 사용
          new ParameterizedTypeReference<>() {},
          testGym.getId()
      );

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      List<ProfileResponseDto> responseBody = response.getBody();
      assertThat(responseBody).hasSize(2);
      assertThat(responseBody.stream().map(ProfileResponseDto::trainerId))
          .containsExactlyInAnyOrder(testTrainer.getId(), anotherTrainer.getId());
    }
  }
}