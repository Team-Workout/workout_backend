package com.workout.pt.controller;

import com.workout.auth.dto.SigninRequest;
import com.workout.auth.dto.SigninResponse;
import com.workout.global.config.EmbeddedRedisConfig;
import com.workout.global.dto.ApiResponse;
import com.workout.gym.domain.Gym;
import com.workout.gym.repository.GymRepository;
import com.workout.member.domain.AccountStatus;
import com.workout.member.domain.Gender;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import com.workout.pt.domain.contract.*;
import com.workout.pt.dto.request.AppointmentRequest;
import com.workout.pt.dto.request.AppointmentStatusUpdateRequest;
import com.workout.pt.dto.request.AppointmentUpdateRequest;
import com.workout.pt.dto.response.AppointmentResponse;
import com.workout.pt.repository.PTAppointmentRepository;
import com.workout.pt.repository.PTApplicationRepository;
import com.workout.pt.repository.PTContractRepository;
import com.workout.pt.repository.PTOfferingRepository;
import com.workout.trainer.domain.Trainer;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({EmbeddedRedisConfig.class})
@DisplayName("PTAppointmentController 통합 테스트 (Session)")
class PTAppointmentControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired private MemberRepository memberRepository;
  @Autowired private GymRepository gymRepository;
  @Autowired private PTOfferingRepository ptOfferingRepository;
  @Autowired private PTApplicationRepository ptApplicationRepository;
  @Autowired private PTContractRepository ptContractRepository;
  @Autowired private PTAppointmentRepository ptAppointmentRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private Member testMember;
  private Trainer testTrainer;
  private String memberSessionCookie;
  private String trainerSessionCookie;
  private PTContract testContract;

  @BeforeEach
  void setUp() {
    Gym gym = gymRepository.save(Gym.builder().name("테스트짐").build());
    testMember = createMember("member@test.com", "테스트회원", Role.MEMBER, gym);
    testTrainer = (Trainer) createMember("trainer@test.com", "테스트트레이너", Role.TRAINER, gym);
    memberSessionCookie = loginAndGetSessionCookie("member@test.com");
    trainerSessionCookie = loginAndGetSessionCookie("trainer@test.com");

    PTOffering offering = ptOfferingRepository.save(PTOffering.builder()
        .trainer(testTrainer).gym(gym).title("PT 상품").price(1000L)
        .totalSessions(10L).status(PTOfferingStatus.ACTIVE).build());

    PTApplication application = ptApplicationRepository.save(PTApplication.builder()
        .member(testMember).offering(offering).status(PTApplicationStatus.APPROVED).build());

    testContract = ptContractRepository.save(PTContract.builder()
        .member(testMember).trainer(testTrainer).gym(gym)
        .application(application).status(PTContractStatus.ACTIVE)
        .remainingSessions(10L).build());
  }

  @AfterEach
  void tearDown() {
    ptAppointmentRepository.deleteAllInBatch();
    ptContractRepository.deleteAllInBatch();
    ptApplicationRepository.deleteAllInBatch();
    ptOfferingRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
    gymRepository.deleteAllInBatch();
  }

  @Nested
  @DisplayName("회원의 PT 희망 시간 제안 (POST /propose)")
  class ProposeAppointment {
    @Test
    @DisplayName("성공: 회원이 PT 시간을 제안하면 201 Created를 반환한다")
    void proposeAppointment_success() {
      AppointmentRequest requestDto = new AppointmentRequest(
          LocalDateTime.now().plusDays(1),
          LocalDateTime.now().plusDays(1).plusHours(1),
          null, null, null, testContract.getId());
      HttpEntity<AppointmentRequest> requestEntity = createHttpEntity(memberSessionCookie, requestDto);

      ResponseEntity<Void> response = restTemplate.postForEntity("/api/pt-appointments/propose", requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getHeaders().getLocation()).isNotNull();
      assertThat(ptAppointmentRepository.count()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("트레이너의 PT 스케줄 생성 (POST /)")
  class CreateAppointment {
    @Test
    @DisplayName("성공: 트레이너가 스케줄을 생성하면 200 OK를 반환한다")
    void createAppointment_success() {
      AppointmentRequest requestDto = new AppointmentRequest(
          LocalDateTime.now().plusDays(2),
          LocalDateTime.now().plusDays(2).plusHours(1),
          null, null, null, testContract.getId());
      HttpEntity<AppointmentRequest> requestEntity = createHttpEntity(trainerSessionCookie, requestDto);

      ResponseEntity<Void> response = restTemplate.postForEntity("/api/pt-appointments", requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(ptAppointmentRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("실패: 회원이 스케줄 생성을 시도하면 403 Forbidden을 반환한다")
    void createAppointment_fail_byMember() {
      AppointmentRequest requestDto = new AppointmentRequest(
          LocalDateTime.now().plusDays(2),
          LocalDateTime.now().plusDays(2).plusHours(1),
          null, null, null, testContract.getId());
      HttpEntity<AppointmentRequest> requestEntity = createHttpEntity(memberSessionCookie, requestDto);

      ResponseEntity<Void> response = restTemplate.postForEntity("/api/pt-appointments", requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("PT 스케줄 상태 변경 (PATCH /{id}/status)")
  class UpdateStatus {
    @Test
    @DisplayName("성공: 트레이너가 예약을 '완료' 상태로 변경 시 200 OK를 반환하고 세션이 차감된다")
    void updateStatus_success() {
      PTAppointment appointment = ptAppointmentRepository.save(PTAppointment.builder()
          .contract(testContract).status(PTAppointmentStatus.SCHEDULED).build());
      AppointmentStatusUpdateRequest requestDto = new AppointmentStatusUpdateRequest(PTAppointmentStatus.COMPLETED);
      HttpEntity<AppointmentStatusUpdateRequest> requestEntity = createHttpEntity(trainerSessionCookie, requestDto);
      long initialSessions = testContract.getRemainingSessions();

      ResponseEntity<Void> response = restTemplate.exchange("/api/pt-appointments/" + appointment.getId() + "/status", HttpMethod.PATCH, requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      PTContract updatedContract = ptContractRepository.findById(testContract.getId()).get();
      assertThat(updatedContract.getRemainingSessions()).isEqualTo(initialSessions - 1);
    }
  }

  @Nested
  @DisplayName("기간별 스케줄 조회 (GET /me/scheduled)")
  class GetScheduledAppointments {
    @Test
    @DisplayName("성공: 유효한 기간으로 조회 시 200 OK와 예약 목록을 반환한다")
    void getMyScheduledAppointments_success() {
      PTAppointment savedAppointment = ptAppointmentRepository.save(PTAppointment.builder()
          .contract(testContract).status(PTAppointmentStatus.SCHEDULED)
          .startTime(LocalDateTime.now().plusDays(2))
          .endTime(LocalDateTime.now().plusDays(2).plusHours(1)).build());
      HttpEntity<Void> requestEntity = createHttpEntity(trainerSessionCookie, null);
      String startDate = LocalDate.now().plusDays(1).toString();
      String endDate = LocalDate.now().plusDays(3).toString();
      String url = String.format("/api/pt-appointments/me/scheduled?startDate=%s&endDate=%s", startDate, endDate);

      ResponseEntity<ApiResponse<List<AppointmentResponse>>> response = restTemplate.exchange(
          url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {});

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).hasSize(1);
      assertThat(response.getBody().data().get(0).id()).isEqualTo(savedAppointment.getId());
    }

    @Test
    @DisplayName("실패: 조회 기간이 7일을 초과하면 400 Bad Request를 반환한다")
    void getMyScheduledAppointments_fail_periodExceeded() {
      HttpEntity<Void> requestEntity = createHttpEntity(trainerSessionCookie, null);
      String startDate = LocalDate.now().toString();
      String endDate = LocalDate.now().plusDays(8).toString();
      String url = String.format("/api/pt-appointments/me/scheduled?startDate=%s&endDate=%s", startDate, endDate);

      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
  }

  // --- Helper Methods ---
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