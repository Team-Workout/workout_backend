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
import com.workout.pt.dto.response.ContractResponse;
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
@DisplayName("PTContractController 통합 테스트 (Session)")
class PTContractControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired private MemberRepository memberRepository;
  @Autowired private GymRepository gymRepository;
  @Autowired private PTOfferingRepository ptOfferingRepository;
  @Autowired private PTApplicationRepository ptApplicationRepository;
  @Autowired private PTContractRepository ptContractRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private Member testMember;
  private Trainer testTrainer;
  private Member anotherMember;
  private String memberCookie;
  private String trainerCookie;
  private String anotherMemberCookie;
  private PTContract activeContract;
  private PTContract completedContract;

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

    PTApplication application1 = ptApplicationRepository.save(PTApplication.builder()
        .member(testMember).offering(offering).status(PTApplicationStatus.APPROVED).build());
    PTApplication application2 = ptApplicationRepository.save(PTApplication.builder()
        .member(testMember).offering(offering).status(PTApplicationStatus.APPROVED).build());

    activeContract = ptContractRepository.save(PTContract.builder()
        .member(testMember).trainer(testTrainer).gym(gym)
        .application(application1).status(PTContractStatus.ACTIVE).build());
    completedContract = ptContractRepository.save(PTContract.builder()
        .member(testMember).trainer(testTrainer).gym(gym)
        .application(application2).status(PTContractStatus.COMPLETED).build());
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
  @DisplayName("나의 계약 목록 조회 (GET /api/pt/contract/me)")
  class GetMyContracts {

    @Test
    @DisplayName("성공: 회원이 자신의 계약 목록 조회 시 200 OK와 페이징 데이터를 반환한다")
    void getMyContracts_successAsMember() {
      HttpEntity<Void> requestEntity = createHttpEntity(memberCookie, null);

      ResponseEntity<ApiResponse<List<ContractResponse>>> response = restTemplate.exchange(
          "/api/pt/contract/me?page=0&size=10", HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {});

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().pageInfo().totalElements()).isEqualTo(2);
      assertThat(response.getBody().data().get(0).memberId()).isEqualTo(testMember.getId());
    }

    @Test
    @DisplayName("성공: 트레이너가 자신의 계약 목록 조회 시 200 OK를 반환한다")
    void getMyContracts_successAsTrainer() {
      HttpEntity<Void> requestEntity = createHttpEntity(trainerCookie, null);

      ResponseEntity<ApiResponse<List<ContractResponse>>> response = restTemplate.exchange(
          "/api/pt/contract/me?page=0&size=10", HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {});

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().pageInfo().totalElements()).isEqualTo(2);
      assertThat(response.getBody().data().get(0).trainerId()).isEqualTo(testTrainer.getId());
    }
  }

  @Nested
  @DisplayName("계약 취소 (DELETE /api/pt/contract/{contractId})")
  class CancelContract {

    @Test
    @DisplayName("성공: 회원이 자신의 활성 계약을 취소 시 204 No Content를 반환한다")
    void cancelContract_successByMember() {
      HttpEntity<Void> requestEntity = createHttpEntity(memberCookie, null);

      ResponseEntity<Void> response = restTemplate.exchange("/api/pt/contract/" + activeContract.getId(), HttpMethod.DELETE, requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      PTContract cancelledContract = ptContractRepository.findById(activeContract.getId()).get();
      assertThat(cancelledContract.getStatus()).isEqualTo(PTContractStatus.CANCELLED);
    }

    @Test
    @DisplayName("실패: 다른 회원이 남의 계약을 취소 시도 시 403 Forbidden을 반환한다")
    void cancelContract_fail_byAnotherMember() {
      HttpEntity<Void> requestEntity = createHttpEntity(anotherMemberCookie, null);

      ResponseEntity<Void> response = restTemplate.exchange("/api/pt/contract/" + activeContract.getId(), HttpMethod.DELETE, requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("실패: 이미 완료된 계약을 취소 시도 시 400 Bad Request를 반환한다")
    void cancelContract_fail_whenAlreadyCompleted() {
      HttpEntity<Void> requestEntity = createHttpEntity(memberCookie, null);

      ResponseEntity<Void> response = restTemplate.exchange("/api/pt/contract/" + completedContract.getId(), HttpMethod.DELETE, requestEntity, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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