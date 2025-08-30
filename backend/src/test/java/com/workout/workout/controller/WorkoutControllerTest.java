package com.workout.workout.controller;

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
import com.workout.pt.repository.*;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.dto.log.WorkoutLogCreateRequest;
import com.workout.workout.dto.log.WorkoutLogResponse;
import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.log.FeedbackRepository;
import com.workout.workout.repository.log.WorkoutExerciseRepository;
import com.workout.workout.repository.log.WorkoutLogRepository;
import com.workout.workout.repository.log.WorkoutSetRepository;
import com.workout.workout.repository.routine.RoutineRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({EmbeddedRedisConfig.class})
@DisplayName("WorkoutController 통합 테스트")
class WorkoutControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired private MemberRepository memberRepository;
  @Autowired private GymRepository gymRepository;
  @Autowired private ExerciseRepository exerciseRepository;
  @Autowired private WorkoutLogRepository workoutLogRepository;
  @Autowired private WorkoutExerciseRepository workoutExerciseRepository;
  @Autowired private WorkoutSetRepository workoutSetRepository;
  @Autowired private FeedbackRepository feedbackRepository;
  @Autowired private RoutineRepository routineRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private PTSessionRepository ptSessionRepository;
  @Autowired private PTAppointmentRepository ptAppointmentRepository;
  @Autowired private PTContractRepository ptContractRepository;
  @Autowired private PTApplicationRepository ptApplicationRepository;
  @Autowired private PTOfferingRepository ptOfferingRepository;

  private Member testUser;
  private Member anotherUser;
  private Exercise benchPress;
  private String testUserCookie;
  private String anotherUserCookie;
  private Gym testGym;

  @BeforeEach
  void setUp() {
    testGym = gymRepository.save(Gym.builder().name("테스트 헬스장").build());
    testUser = createMember("test@example.com", "테스트유저", testGym);
    anotherUser = createMember("another@example.com", "다른유저", testGym);
    benchPress = exerciseRepository.save(Exercise.builder().name("벤치프레스").build());

    testUserCookie = loginAndGetSessionCookie("test@example.com");
    anotherUserCookie = loginAndGetSessionCookie("another@example.com");
  }

  @AfterEach
  void tearDown() {
    ptSessionRepository.deleteAllInBatch();
    feedbackRepository.deleteAllInBatch();
    workoutSetRepository.deleteAllInBatch();
    workoutExerciseRepository.deleteAllInBatch();
    ptAppointmentRepository.deleteAllInBatch();
    ptContractRepository.deleteAllInBatch();
    ptApplicationRepository.deleteAllInBatch();
    ptOfferingRepository.deleteAllInBatch();
    workoutLogRepository.deleteAllInBatch();
    routineRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
    gymRepository.deleteAllInBatch();
    exerciseRepository.deleteAllInBatch();
  }

  private Member createMember(String email, String name, Gym gym) {
    return memberRepository.save(Member.builder()
        .email(email).name(name).password(passwordEncoder.encode("password123"))
        .gym(gym).gender(Gender.MALE).role(Role.MEMBER).accountStatus(AccountStatus.ACTIVE)
        .build());
  }

  private String loginAndGetSessionCookie(String email) {
    SigninRequest loginDto = new SigninRequest(email, "password123");
    ResponseEntity<SigninResponse> response = restTemplate.postForEntity("/api/auth/signin", loginDto, SigninResponse.class);
    return response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
  }

  private <T> HttpEntity<T> createHttpEntity(String cookie, T body) {
    HttpHeaders headers = new HttpHeaders();
    if (cookie != null) {
      headers.add(HttpHeaders.COOKIE, cookie);
    }
    if (body != null) {
      headers.setContentType(MediaType.APPLICATION_JSON);
      return new HttpEntity<>(body, headers);
    }
    return new HttpEntity<>(headers);
  }


  @Nested
  @DisplayName("운동일지 생성 (POST /api/workout/logs)")
  class CreateWorkoutLog {
    @Test
    @DisplayName("성공: 인증된 사용자가 운동일지 생성 시 201 Created와 Location 헤더를 반환한다")
    void createWorkoutLog_success() {
      WorkoutLogCreateRequest requestDto = new WorkoutLogCreateRequest(LocalDate.now(), "테스트 피드백", List.of(
          new WorkoutLogCreateRequest.WorkoutExerciseDto(benchPress.getId(), 1, List.of(
              new WorkoutLogCreateRequest.WorkoutSetDto(1, new BigDecimal("100"), 5, "피드백")
          ))
      ));
      HttpEntity<WorkoutLogCreateRequest> requestEntity = createHttpEntity(testUserCookie, requestDto);

      ResponseEntity<Void> response = restTemplate.postForEntity("/api/workout/logs", requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getHeaders().getLocation()).isNotNull();
      assertThat(workoutLogRepository.count()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("운동일지 상세 조회 (GET /api/workout/logs/{id})")
  class GetWorkoutLog {
    @Test
    @DisplayName("성공: 자신의 운동일지 ID로 상세 조회에 성공하면 200 OK와 데이터를 반환한다")
    void getWorkoutLog_success() {
      WorkoutLog savedLog = workoutLogRepository.save(WorkoutLog.builder().member(testUser).workoutDate(LocalDate.now()).build());
      HttpEntity<Void> requestEntity = createHttpEntity(testUserCookie, null);

      ResponseEntity<WorkoutLogResponse> response = restTemplate.exchange(
          "/api/workout/logs/" + savedLog.getId(), HttpMethod.GET, requestEntity, WorkoutLogResponse.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().workoutLogId()).isEqualTo(savedLog.getId());
    }

    @Test
    @DisplayName("실패(보안): 다른 사용자의 운동일지 조회 시 403 Forbidden을 반환해야 한다")
    void getWorkoutLog_fail_unauthorized() {
      WorkoutLog savedLog = workoutLogRepository.save(WorkoutLog.builder().member(testUser).workoutDate(LocalDate.now()).build());
      HttpEntity<Void> requestEntity = createHttpEntity(anotherUserCookie, null);

      ResponseEntity<WorkoutLogResponse> response = restTemplate.exchange(
          "/api/workout/logs/" + savedLog.getId(), HttpMethod.GET, requestEntity, WorkoutLogResponse.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("운동일지 삭제 (DELETE /api/workout/logs/{id})")
  class DeleteWorkoutLog {
    @Test
    @DisplayName("성공: 자신의 운동일지를 삭제하면 204 No Content를 반환한다")
    void deleteWorkoutLog_success() {
      WorkoutLog savedLog = workoutLogRepository.save(WorkoutLog.builder().member(testUser).workoutDate(LocalDate.now()).build());
      HttpEntity<Void> requestEntity = createHttpEntity(testUserCookie, null);

      ResponseEntity<Void> response = restTemplate.exchange("/api/workout/logs/" + savedLog.getId(), HttpMethod.DELETE, requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      assertThat(workoutLogRepository.existsById(savedLog.getId())).isFalse();
    }

    @Test
    @DisplayName("실패: 다른 사용자의 운동일지를 삭제하려 하면 403 Forbidden을 반환한다")
    void deleteWorkoutLog_fail_unauthorized() {
      WorkoutLog savedLog = workoutLogRepository.save(WorkoutLog.builder().member(testUser).workoutDate(LocalDate.now()).build());
      HttpEntity<Void> requestEntity = createHttpEntity(anotherUserCookie, null);

      ResponseEntity<Void> response = restTemplate.exchange("/api/workout/logs/" + savedLog.getId(), HttpMethod.DELETE, requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
  }
}