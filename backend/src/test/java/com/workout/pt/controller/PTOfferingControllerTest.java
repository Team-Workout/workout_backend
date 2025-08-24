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
import com.workout.pt.dto.request.OfferingCreateRequest;
import com.workout.pt.repository.PTOfferingRepository;
import com.workout.trainer.domain.Trainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("PTOfferingController 통합 테스트 (Session)")
class PTOfferingControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private GymRepository gymRepository;
  @Autowired
  private PTOfferingRepository ptOfferingRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  private Trainer testTrainer;
  private String trainerSessionCookie;
  private String memberSessionCookie;

  @BeforeEach
  void setUp() {
    testTrainer = (Trainer) createMember("trainer@test.com", "테스트트레이너", Role.TRAINER);
    createMember("member@test.com", "테스트회원", Role.MEMBER);

    trainerSessionCookie = loginAndGetSessionCookie("trainer@test.com");
    memberSessionCookie = loginAndGetSessionCookie("member@test.com");
  }

  @AfterEach
  void tearDown() {
    ptOfferingRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
    gymRepository.deleteAllInBatch();
  }

  // [FIXED] Member와 Trainer 생성 로직 수정
  private Member createMember(String email, String name, Role role) {
    Gym gym = gymRepository.save(Gym.builder().name("테스트짐").build());

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

  @Test
  @DisplayName("성공: 트레이너가 PT 상품 생성 시 201 Created를 반환한다")
  void createOffering_success() {
    // given
    OfferingCreateRequest requestDto = new OfferingCreateRequest("PT 10회", "설명", 500000L, 10L, 3L);
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, trainerSessionCookie);
    HttpEntity<OfferingCreateRequest> requestEntity = new HttpEntity<>(requestDto, headers);

    // when
    ResponseEntity<Void> response = restTemplate.postForEntity("/api/pt-offerings", requestEntity, Void.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(ptOfferingRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("실패: 일반 회원이 PT 상품 생성 시 403 Forbidden을 반환한다")
  void createOffering_fail_byMember() {
    // given
    OfferingCreateRequest requestDto = new OfferingCreateRequest("PT 10회", "설명", 500000L, 10L, 3L);
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, memberSessionCookie);
    HttpEntity<OfferingCreateRequest> requestEntity = new HttpEntity<>(requestDto, headers);

    // when
    ResponseEntity<Void> response = restTemplate.postForEntity("/api/pt-offerings", requestEntity, Void.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}