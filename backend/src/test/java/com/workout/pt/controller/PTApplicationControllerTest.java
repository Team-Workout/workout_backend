package com.workout.pt.controller;

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
import com.workout.pt.domain.contract.PTApplication;
import com.workout.pt.domain.contract.PTApplicationStatus;
import com.workout.pt.domain.contract.PTOffering;
import com.workout.pt.domain.contract.PTOfferingStatus;
import com.workout.pt.dto.request.PtApplicationRequest;
import com.workout.pt.dto.response.PendingApplicationResponse;
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
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({EmbeddedRedisConfig.class})
@DisplayName("PTApplicationController 통합 테스트 (Session)")
class PTApplicationControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private GymRepository gymRepository;
  @Autowired
  private PTOfferingRepository ptOfferingRepository;
  @Autowired
  private PTApplicationRepository ptApplicationRepository;
  @Autowired
  private PTContractRepository ptContractRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  private Member testMember1, anotherMember;
  private Trainer testTrainer1, anotherTrainer;
  private String member1Cookie, anotherMemberCookie, trainer1Cookie, anotherTrainerCookie;
  private PTOffering offeringFromTrainer1;

  @BeforeEach
  void setUp() {
    Gym gym1 = gymRepository.save(Gym.builder().name("테스트짐 1").build());
    Gym gym2 = gymRepository.save(Gym.builder().name("테스트짐 2").build());

    testMember1 = createMember("member1@test.com", "테스트회원1", Role.MEMBER, gym1);
    anotherMember = createMember("member2@test.com", "다른회원", Role.MEMBER, gym2); // [FIXED]
    testTrainer1 = (Trainer) createMember("trainer1@test.com", "테스트트레이너1", Role.TRAINER, gym1);
    anotherTrainer = (Trainer) createMember("trainer2@test.com", "다른트레이너", Role.TRAINER, gym2); // [FIXED]

    member1Cookie = loginAndGetSessionCookie("member1@test.com");
    anotherMemberCookie = loginAndGetSessionCookie("member2@test.com"); // [FIXED]
    trainer1Cookie = loginAndGetSessionCookie("trainer1@test.com");
    anotherTrainerCookie = loginAndGetSessionCookie("trainer2@test.com"); // [FIXED]

    offeringFromTrainer1 = ptOfferingRepository.save(PTOffering.builder()
        .trainer(testTrainer1).gym(gym1).title("PT 상품").price(1000L)
        .totalSessions(10L).status(PTOfferingStatus.ACTIVE).build());
  }

  @AfterEach
  void tearDown() {
    ptContractRepository.deleteAllInBatch();
    ptApplicationRepository.deleteAllInBatch();
    ptOfferingRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
    gymRepository.deleteAllInBatch();
  }

  private Member createMember(String email, String name, Role role, Gym gym) {
    if (role == Role.TRAINER) {
      Trainer trainer = Trainer.builder()
          .email(email).name(name).gym(gym)
          .password(passwordEncoder.encode("password123"))
          .gender(Gender.MALE)
          .accountStatus(AccountStatus.ACTIVE)
          .build();
      return memberRepository.save(trainer);
    } else {
      Member member = Member.builder()
          .email(email).name(name).gym(gym)
          .password(passwordEncoder.encode("password123"))
          .gender(Gender.MALE)
          .accountStatus(AccountStatus.ACTIVE)
          .build();
      return memberRepository.save(member);
    }
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

  private PTApplication createApp(Member member, PTOffering offering, PTApplicationStatus status) {
    return ptApplicationRepository.save(PTApplication.builder()
        .member(member).offering(offering).status(status).build());
  }

  @Nested
  @DisplayName("PT 신청 생성 (POST /api/pt-applications)")
  class CreateApplication {
    @Test
    @DisplayName("성공: 회원이 PT 신청 시 200 OK를 반환한다")
    void createApplication_success() {
      PtApplicationRequest requestDto = new PtApplicationRequest(offeringFromTrainer1.getId());
      HttpEntity<PtApplicationRequest> requestEntity = createHttpEntity(member1Cookie, requestDto);

      ResponseEntity<Void> response = restTemplate.postForEntity("/api/pt-applications", requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(ptApplicationRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 PT 상품 ID로 신청 시 404 Not Found를 반환한다")
    void createApplication_fail_offeringNotFound() {
      PtApplicationRequest requestDto = new PtApplicationRequest(9999L);
      HttpEntity<PtApplicationRequest> requestEntity = createHttpEntity(member1Cookie, requestDto);

      ResponseEntity<Void> response = restTemplate.postForEntity("/api/pt-applications", requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("PT 신청 처리 (수락/거절/취소)")
  class ProcessApplication {
    @Test
    @DisplayName("성공: 트레이너가 자신에게 온 신청을 수락 시 200 OK와 함께 계약이 생성된다")
    void acceptApplication_success() {
      PTApplication application = createApp(testMember1, offeringFromTrainer1, PTApplicationStatus.PENDING);
      HttpEntity<Void> requestEntity = createHttpEntity(trainer1Cookie, null);

      ResponseEntity<Void> response = restTemplate.exchange("/api/pt-applications/" + application.getId() + "/acceptance", HttpMethod.PATCH, requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      PTApplication acceptedApp = ptApplicationRepository.findById(application.getId()).get();
      assertThat(acceptedApp.getStatus()).isEqualTo(PTApplicationStatus.APPROVED);
      assertThat(ptContractRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("실패: 다른 트레이너가 수락 시도 시 403 Forbidden을 반환한다")
    void acceptApplication_fail_byAnotherTrainer() {
      PTApplication application = createApp(testMember1, offeringFromTrainer1, PTApplicationStatus.PENDING);
      HttpEntity<Void> requestEntity = createHttpEntity(anotherTrainerCookie, null);

      ResponseEntity<Void> response = restTemplate.exchange("/api/pt-applications/" + application.getId() + "/acceptance", HttpMethod.PATCH, requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("실패: 이미 처리된(APPROVED) 신청을 다시 수락 시 400 Bad Request를 반환한다")
    void acceptApplication_fail_whenAlreadyApproved() {
      PTApplication application = createApp(testMember1, offeringFromTrainer1, PTApplicationStatus.APPROVED);
      HttpEntity<Void> requestEntity = createHttpEntity(trainer1Cookie, null);

      ResponseEntity<Void> response = restTemplate.exchange("/api/pt-applications/" + application.getId() + "/acceptance", HttpMethod.PATCH, requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("성공: 트레이너가 자신에게 온 신청을 거절 시 200 OK를 반환한다")
    void rejectApplication_success() {
      PTApplication application = createApp(testMember1, offeringFromTrainer1, PTApplicationStatus.PENDING);
      HttpEntity<Void> requestEntity = createHttpEntity(trainer1Cookie, null);

      ResponseEntity<Void> response = restTemplate.exchange("/api/pt-applications/" + application.getId() + "/rejection", HttpMethod.PATCH, requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      PTApplication rejectedApp = ptApplicationRepository.findById(application.getId()).get();
      assertThat(rejectedApp.getStatus()).isEqualTo(PTApplicationStatus.REJECTED);
      assertThat(ptContractRepository.count()).isZero();
    }

    @Test
    @DisplayName("성공: 회원이 자신의 PENDING 상태인 신청을 취소 시 200 OK를 반환한다")
    void cancelApplication_success() {
      PTApplication application = createApp(testMember1, offeringFromTrainer1, PTApplicationStatus.PENDING);
      HttpEntity<Void> requestEntity = createHttpEntity(member1Cookie, null);

      ResponseEntity<Void> response = restTemplate.exchange("/api/pt-applications/" + application.getId() + "/cancellation", HttpMethod.PATCH, requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      PTApplication cancelledApp = ptApplicationRepository.findById(application.getId()).get();
      assertThat(cancelledApp.getStatus()).isEqualTo(PTApplicationStatus.CANCELLED);
    }

    @Test
    @DisplayName("실패: 다른 회원이 남의 신청을 취소 시 403 Forbidden을 반환한다")
    void cancelApplication_fail_byAnotherMember() {
      PTApplication application = createApp(testMember1, offeringFromTrainer1, PTApplicationStatus.PENDING);
      HttpEntity<Void> requestEntity = createHttpEntity(anotherMemberCookie, null);

      ResponseEntity<Void> response = restTemplate.exchange("/api/pt-applications/" + application.getId() + "/cancellation", HttpMethod.PATCH, requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("PT 신청 목록 조회 (GET /api/pt-applications)")
  class GetApplications {
    @Test
    @DisplayName("성공: 회원이 자신의 신청 목록 조회 시 200 OK와 자신의 신청 1건을 반환한다")
    void getApplications_success_asMember() {
      createApp(testMember1, offeringFromTrainer1, PTApplicationStatus.PENDING);
      createApp(anotherMember, offeringFromTrainer1, PTApplicationStatus.PENDING);
      HttpEntity<Void> requestEntity = createHttpEntity(member1Cookie, null);

      ResponseEntity<PendingApplicationResponse> response = restTemplate.exchange("/api/pt-applications", HttpMethod.GET, requestEntity, PendingApplicationResponse.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().applications()).hasSize(1);
      assertThat(response.getBody().applications().get(0).memberName()).isEqualTo("테스트회원1");
    }

    @Test
    @DisplayName("성공: 트레이너가 자신에게 온 신청 목록 조회 시 200 OK와 2건을 반환한다")
    void getApplications_success_asTrainer() {
      createApp(testMember1, offeringFromTrainer1, PTApplicationStatus.PENDING);
      createApp(anotherMember, offeringFromTrainer1, PTApplicationStatus.PENDING);
      HttpEntity<Void> requestEntity = createHttpEntity(trainer1Cookie, null);

      ResponseEntity<PendingApplicationResponse> response = restTemplate.exchange("/api/pt-applications", HttpMethod.GET, requestEntity, PendingApplicationResponse.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().applications()).hasSize(2);
    }

    @Test
    @DisplayName("실패: 인증되지 않은 사용자가 조회 시 401 Unauthorized를 반환한다")
    void getApplications_fail_unauthorized() {
      HttpEntity<Void> requestEntity = createHttpEntity(null, null);

      ResponseEntity<PendingApplicationResponse> response = restTemplate.exchange("/api/pt-applications", HttpMethod.GET, requestEntity, PendingApplicationResponse.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
  }
}