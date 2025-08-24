package com.workout.pt.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.gym.domain.Gym; // Gym import 추가
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import com.workout.pt.dto.response.ClientListResponse.MemberResponse;
import com.workout.pt.repository.PTContractRepository;
import com.workout.pt.service.contract.PTTrainerService;
import com.workout.trainer.domain.Trainer;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("PTTrainerService 단위 테스트")
class PTTrainerServiceTest {

  @InjectMocks
  private PTTrainerService ptTrainerService;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PTContractRepository ptContractRepository;

  private Trainer testTrainer;
  private Member testMember1;
  private Member testMember2;
  private UserPrincipal trainerPrincipal;
  private UserPrincipal memberPrincipal;
  private Pageable pageable;
  private Gym testGym; // [FIX] 테스트용 Gym 객체 추가

  @BeforeEach
  void setUp() {
    // [FIX] 테스트용 Gym 객체 생성
    testGym = Gym.builder().id(1L).name("테스트 헬스장").build();

    // [FIX] Member와 Trainer 생성 시 .gym(testGym)을 추가하여 NullPointerException 방지
    testTrainer = Trainer.builder().id(1L).role(Role.TRAINER).name("테스트트레이너").gym(testGym).build();
    testMember1 = Member.builder().id(101L).role(Role.MEMBER).name("회원1").gym(testGym).build();
    testMember2 = Member.builder().id(102L).role(Role.MEMBER).name("회원2").gym(testGym).build();

    trainerPrincipal = new UserPrincipal(testTrainer);
    memberPrincipal = new UserPrincipal(testMember1);
    pageable = PageRequest.of(0, 10);
  }

  @Nested
  @DisplayName("나의 클라이언트 조회 (findMyClients)")
  class FindMyClientsTest {

    @Test
    @DisplayName("성공: 트레이너가 자신의 활성 회원 목록을 조회한다")
    void findMyClients_success() {
      // given
      PTContract contract1 = PTContract.builder().trainer(testTrainer).member(testMember1).build();
      PTContract contract2 = PTContract.builder().trainer(testTrainer).member(testMember2).build();
      Page<PTContract> contractPage = new PageImpl<>(List.of(contract1, contract2), pageable, 2);

      given(memberRepository.findById(testTrainer.getId())).willReturn(Optional.of(testTrainer));
      given(ptContractRepository.findByTrainerIdAndStatus(testTrainer.getId(), PTContractStatus.ACTIVE, pageable))
          .willReturn(contractPage);

      // when
      Page<MemberResponse> responsePage = ptTrainerService.findMyClients(trainerPrincipal, pageable);

      // then
      assertThat(responsePage.getContent()).hasSize(2);
      assertThat(responsePage.getContent()).extracting("name").containsExactlyInAnyOrder("회원1", "회원2");
      assertThat(responsePage.getContent().get(0).gymName()).isEqualTo("테스트 헬스장");
    }

    @Test
    @DisplayName("실패: 일반 회원이 클라이언트 조회를 시도하면 AccessDeniedException이 발생한다")
    void findMyClients_fail_whenNotTrainer() {
      // given
      given(memberRepository.findById(testMember1.getId())).willReturn(Optional.of(testMember1));

      // when & then
      assertThatThrownBy(() -> ptTrainerService.findMyClients(memberPrincipal, pageable))
          .isInstanceOf(AccessDeniedException.class)
          .hasMessage("트레이너만 이용할 수 있는 서비스입니다.");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 트레이너 ID로 요청 시 EntityNotFoundException이 발생한다")
    void findMyClients_fail_whenTrainerNotFound() {
      // given
      given(memberRepository.findById(testTrainer.getId())).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> ptTrainerService.findMyClients(trainerPrincipal, pageable))
          .isInstanceOf(EntityNotFoundException.class);
    }
  }
}