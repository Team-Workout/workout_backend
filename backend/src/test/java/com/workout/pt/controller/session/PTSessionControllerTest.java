package com.workout.pt.controller.session;

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
import com.workout.pt.domain.contract.*;
import com.workout.pt.domain.session.PTSession;
import com.workout.pt.dto.request.PTSessionCreateRequest;
import com.workout.pt.repository.*;
import com.workout.trainer.domain.Trainer;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.dto.log.WorkoutLogCreateRequest;
import com.workout.workout.repository.log.WorkoutLogRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({EmbeddedRedisConfig.class})
@DisplayName("PTSessionController 통합 테스트 (Session)")
class PTSessionControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired private MemberRepository memberRepository;
  @Autowired private GymRepository gymRepository;
  @Autowired private PTOfferingRepository ptOfferingRepository;
  @Autowired private PTApplicationRepository ptApplicationRepository;
  @Autowired private PTContractRepository ptContractRepository;
  @Autowired private PTAppointmentRepository ptAppointmentRepository;
  @Autowired private PTSessionRepository ptSessionRepository;
  @Autowired private WorkoutLogRepository workoutLogRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private Member testMember;
  private Trainer testTrainer;
  private Member anotherMember;
  private String memberCookie;
  private String trainerCookie;
  private String anotherMemberCookie;
  private PTAppointment scheduledAppointment;

  @BeforeEach
  void setUp() {
    Gym gym = gymRepository.save(Gym.builder().name("테스트짐").build());
    testMember = createMember("member@test.com", "테스트회원", Role.MEMBER, gym);
    anotherMember = createMember("another@test.com", "다른회원", Role.MEMBER, gym);
    testTrainer = (Trainer) createMember("trainer@test.com", "테스트트레이너", Role.TRAINER, gym);

    memberCookie = loginAndGetSessionCookie("member@test.com");
    trainerCookie = loginAndGetSessionCookie("trainer@test.com");
    anotherMemberCookie = loginAndGetSessionCookie("another@test.com");

    PTOffering offering = ptOfferingRepository.save(PTOffering.builder()
        .trainer(testTrainer).gym(gym).title("PT 상품").price(1000L)
        .totalSessions(10L).status(PTOfferingStatus.ACTIVE).build());

    PTApplication application = ptApplicationRepository.save(PTApplication.builder()
        .member(testMember).offering(offering).status(PTApplicationStatus.APPROVED).build());

    PTContract contract = ptContractRepository.save(PTContract.builder()
        .member(testMember).trainer(testTrainer).gym(gym)
        .application(application).status(PTContractStatus.ACTIVE).remainingSessions(10L).build());

    scheduledAppointment = ptAppointmentRepository.save(PTAppointment.builder()
        .contract(contract).status(PTAppointmentStatus.SCHEDULED)
        .startTime(LocalDateTime.now().minusDays(1))
        .endTime(LocalDateTime.now().minusDays(1).plusHours(1)).build());
  }

  @AfterEach
  void tearDown() {
    ptSessionRepository.deleteAllInBatch();
    ptAppointmentRepository.deleteAllInBatch();
    workoutLogRepository.deleteAllInBatch();
    ptContractRepository.deleteAllInBatch();
    ptApplicationRepository.deleteAllInBatch();
    ptOfferingRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
    gymRepository.deleteAllInBatch();
  }

  @Nested
  @DisplayName("PT 세션 생성 (POST /api/pt-sessions)")
  class CreatePTSession {
    @Test
    @DisplayName("성공: 트레이너가 요청 시 201 Created와 함께 모든 로직이 실행된다")
    void createPTSession_success() {
      WorkoutLogCreateRequest workoutLogDto = new WorkoutLogCreateRequest(LocalDate.now(), null, Collections.emptyList());
      PTSessionCreateRequest requestDto = new PTSessionCreateRequest(scheduledAppointment.getId(), workoutLogDto);
      HttpEntity<PTSessionCreateRequest> requestEntity = createHttpEntity(trainerCookie, requestDto);

      long initialSessions = scheduledAppointment.getContract().getRemainingSessions();

      ResponseEntity<Void> response = restTemplate.postForEntity("/api/pt-sessions", requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(ptSessionRepository.count()).isEqualTo(1);
      assertThat(workoutLogRepository.count()).isEqualTo(1);

      PTAppointment completedAppointment = ptAppointmentRepository.findById(scheduledAppointment.getId()).get();
      assertThat(completedAppointment.getStatus()).isEqualTo(PTAppointmentStatus.COMPLETED);

      PTContract updatedContract = ptContractRepository.findById(completedAppointment.getContract().getId()).get();
      assertThat(updatedContract.getRemainingSessions()).isEqualTo(initialSessions - 1);
    }

    @Test
    @DisplayName("실패: 회원이 직접 PT 세션 생성을 시도하면 403 Forbidden을 반환한다")
    void createPTSession_fail_byMember() {
      WorkoutLogCreateRequest workoutLogDto = new WorkoutLogCreateRequest(LocalDate.now(), null, Collections.emptyList());
      PTSessionCreateRequest requestDto = new PTSessionCreateRequest(scheduledAppointment.getId(), workoutLogDto);
      HttpEntity<PTSessionCreateRequest> requestEntity = createHttpEntity(memberCookie, requestDto);

      ResponseEntity<Void> response = restTemplate.postForEntity("/api/pt-sessions", requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
  }

  private Member createMember(String email, String name, Role role, Gym gym) {
    if (role == Role.TRAINER) {
      return memberRepository.save(Trainer.builder()
          .email(email).name(name).gym(gym)
          .password(passwordEncoder.encode("password123"))
          .gender(Gender.MALE).accountStatus(AccountStatus.ACTIVE).build());
    }
    return memberRepository.save(Member.builder()
        .email(email).name(name).gym(gym)
        .password(passwordEncoder.encode("password123"))
        .gender(Gender.MALE).accountStatus(AccountStatus.ACTIVE).build());
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
}