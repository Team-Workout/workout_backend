package com.workout.workout.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.workout.auth.dto.SigninRequest;
import com.workout.auth.dto.SigninResponse;
import com.workout.global.config.EmbeddedRedisConfig;
import com.workout.gym.domain.Gym;
import com.workout.gym.repository.GymRepository;
import com.workout.member.domain.AccountStatus;
import com.workout.member.domain.Gender;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.dto.log.WorkoutLogCreateRequest;
import com.workout.workout.dto.log.WorkoutLogResponse;
import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.log.WorkoutLogRepository;
import com.workout.workout.repository.routine.RoutineRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
@DisplayName("WorkoutController 통합 테스트")
class WorkoutControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private GymRepository gymRepository;
  @Autowired
  private ExerciseRepository exerciseRepository;
  @Autowired
  private WorkoutLogRepository workoutLogRepository;
  @Autowired
  private RoutineRepository routineRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  private Member testUser;
  private Exercise benchPress;
  private Exercise squat;
  private String testUserSessionCookie;

  @BeforeEach
  void setUp() {
    testUser = createUser("test@example.com", "테스트유저");
    createUser("another@example.com", "다른유저");

    benchPress = exerciseRepository.save(Exercise.builder().name("벤치프레스").build());
    squat = exerciseRepository.save(Exercise.builder().name("스쿼트").build());

    testUserSessionCookie = loginAndGetSessionCookie("test@example.com", "password123");
  }

  @AfterEach
  void tearDown() {
    // 테스트 데이터 정리 (외래 키 제약조건 고려)
    workoutLogRepository.deleteAllInBatch();
    routineRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
    gymRepository.deleteAllInBatch();
    exerciseRepository.deleteAllInBatch();
  }

  private Member createUser(String email, String name) {
    Gym testGym = gymRepository.save(Gym.builder().name("테스트 헬스장").build());
    Member user = Member.builder()
        .email(email)
        .name(name)
        .password(passwordEncoder.encode("password123"))
        .gym(testGym).gender(Gender.MALE).role(Role.MEMBER).accountStatus(AccountStatus.ACTIVE)
        .build();
    return memberRepository.save(user);
  }

  private String loginAndGetSessionCookie(String email, String password) {
    SigninRequest loginDto = new SigninRequest(email, password);
    ResponseEntity<SigninResponse> response = restTemplate.postForEntity("/api/auth/signin",
        loginDto, SigninResponse.class);
    return response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
  }

  @Nested
  @DisplayName("운동일지 API (/api/workout/logs)")
  class WorkoutLogApiTest {

    @Test
    @DisplayName("성공: 인증된 사용자가 운동일지 생성 시 201 Created를 반환한다")
    void createWorkoutLog_success() {
      // given
      WorkoutLogCreateRequest requestDto = new WorkoutLogCreateRequest(
          LocalDate.now(),
          "테스트 로그 피드백",
          List.of(
              new WorkoutLogCreateRequest.WorkoutExerciseDto(
                  benchPress.getId(),
                  1,
                  List.of(new WorkoutLogCreateRequest.WorkoutSetDto(1, new BigDecimal("100"), 5,
                      "첫 세트 피드백"))
              )
          )
      );
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.COOKIE, testUserSessionCookie);
      HttpEntity<WorkoutLogCreateRequest> requestEntity = new HttpEntity<>(requestDto, headers);

      // when
      ResponseEntity<Void> response = restTemplate.postForEntity("/api/workout/logs", requestEntity,
          Void.class);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getHeaders().getLocation()).isNotNull();
      assertThat(workoutLogRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("성공: 운동일지 ID로 상세 조회에 성공하면 200 OK와 데이터를 반환한다")
    void getWorkoutLog_success() {
      // given
      // [수정] new WorkoutLog(...) 대신 빌더 패턴으로 객체를 생성합니다.
      WorkoutLog logToSave = WorkoutLog.builder()
          .member(testUser)
          .workoutDate(LocalDate.now())
          .build();
      WorkoutLog savedLog = workoutLogRepository.save(logToSave);

      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.COOKIE, testUserSessionCookie);
      HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

      // when
      ResponseEntity<WorkoutLogResponse> response = restTemplate.exchange(
          "/api/workout/logs/" + savedLog.getId(), HttpMethod.GET, requestEntity,
          WorkoutLogResponse.class
      );

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().workoutLogId()).isEqualTo(savedLog.getId());
    }
  }

  // ... RoutineApiTest는 변경 없음 ...
}