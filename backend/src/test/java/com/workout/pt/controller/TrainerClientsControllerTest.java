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
import com.workout.pt.dto.response.ClientListResponse.MemberResponse;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({EmbeddedRedisConfig.class})
@DisplayName("TrainerClientsController 통합 테스트 (Session)")
class TrainerClientsControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired private MemberRepository memberRepository;
  @Autowired private GymRepository gymRepository;
  @Autowired private PTOfferingRepository ptOfferingRepository;
  @Autowired private PTApplicationRepository ptApplicationRepository;
  @Autowired private PTContractRepository ptContractRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private Trainer testTrainer;
  private String trainerSessionCookie;
  private String memberSessionCookie;

  @BeforeEach
  void setUp() {
    Gym gym = gymRepository.save(Gym.builder().name("테스트짐").build());
    Member testMember1 = createMember("member1@test.com", "활성회원1", Role.MEMBER, gym);
    Member testMember2 = createMember("member2@test.com", "활성회원2", Role.MEMBER, gym);
    Member completedMember = createMember("member3@test.com", "종료회원", Role.MEMBER, gym);
    testTrainer = (Trainer) createMember("trainer@test.com", "테스트트레이너", Role.TRAINER, gym);

    trainerSessionCookie = loginAndGetSessionCookie("trainer@test.com");
    memberSessionCookie = loginAndGetSessionCookie("member1@test.com");

    PTOffering offering = ptOfferingRepository.save(PTOffering.builder()
        .trainer(testTrainer).gym(gym).title("PT 상품").price(1000L)
        .totalSessions(10L).status(PTOfferingStatus.ACTIVE).build());

    PTApplication app1 = ptApplicationRepository.save(PTApplication.builder().member(testMember1).offering(offering).status(PTApplicationStatus.APPROVED).build());
    PTApplication app2 = ptApplicationRepository.save(PTApplication.builder().member(testMember2).offering(offering).status(PTApplicationStatus.APPROVED).build());
    PTApplication app3 = ptApplicationRepository.save(PTApplication.builder().member(completedMember).offering(offering).status(PTApplicationStatus.APPROVED).build());

    ptContractRepository.save(PTContract.builder().member(testMember1).trainer(testTrainer).gym(gym).application(app1).status(PTContractStatus.ACTIVE).build());
    ptContractRepository.save(PTContract.builder().member(testMember2).trainer(testTrainer).gym(gym).application(app2).status(PTContractStatus.ACTIVE).build());
    ptContractRepository.save(PTContract.builder().member(completedMember).trainer(testTrainer).gym(gym).application(app3).status(PTContractStatus.COMPLETED).build());
  }

  @AfterEach
  void tearDown() {
    ptContractRepository.deleteAllInBatch();
    ptApplicationRepository.deleteAllInBatch();
    ptOfferingRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
    gymRepository.deleteAllInBatch();
  }

  @Nested
  @DisplayName("관리 회원 목록 조회 (GET /api/trainer/clients/me)")
  class GetMyClients {

    @Test
    @DisplayName("성공: 트레이너가 자신의 활성(ACTIVE) 회원 목록 조회 시 200 OK와 2명의 회원 정보를 반환한다")
    void getMyClients_success() {
      HttpEntity<Void> requestEntity = createHttpEntity(trainerSessionCookie, null);

      ResponseEntity<ApiResponse<List<MemberResponse>>> response = restTemplate.exchange(
          "/api/trainer/clients/me?page=0&size=10",
          HttpMethod.GET,
          requestEntity,
          new ParameterizedTypeReference<>() {});

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().pageInfo().totalElements()).isEqualTo(2);
      assertThat(response.getBody().data()).extracting("name").containsExactlyInAnyOrder("활성회원1", "활성회원2");
    }

    @Test
    @DisplayName("실패: 일반 회원이 클라이언트 목록 조회 시 403 Forbidden을 반환한다")
    void getMyClients_fail_byMember() {
      HttpEntity<Void> requestEntity = createHttpEntity(memberSessionCookie, null);

      ResponseEntity<String> response = restTemplate.exchange("/api/trainer/clients/me", HttpMethod.GET, requestEntity, String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("실패: 인증되지 않은 사용자가 조회 시 401 Unauthorized를 반환한다")
    void getMyClients_fail_unauthorized() {
      HttpEntity<Void> requestEntity = createHttpEntity(null, null);

      ResponseEntity<String> response = restTemplate.exchange("/api/trainer/clients/me", HttpMethod.GET, requestEntity, String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
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
}