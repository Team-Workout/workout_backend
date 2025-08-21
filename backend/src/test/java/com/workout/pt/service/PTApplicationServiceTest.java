package com.workout.pt.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import com.workout.pt.domain.contract.PTApplication;
import com.workout.pt.domain.contract.PTApplicationStatus;
import com.workout.pt.domain.contract.PTOffering;
import com.workout.pt.repository.PTApplicationRepository;
import com.workout.pt.repository.PTOfferingRepository;
import com.workout.trainer.domain.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("PTApplicationService 단위 테스트")
class PTApplicationServiceTest {

  @InjectMocks
  private PTApplicationService ptApplicationService;

  @Mock
  private PTApplicationRepository ptApplicationRepository;
  @Mock
  private PTContractService ptContractService;
  @Mock
  private MemberRepository memberRepository;
  @Mock
  private PTOfferingRepository ptOfferingRepository;

  private Member testMember;
  private Trainer testTrainer;
  private UserPrincipal memberPrincipal;
  private UserPrincipal trainerPrincipal;
  private PTOffering testOffering;
  private PTApplication testApplication;

  @BeforeEach
  void setUp() {
    testMember = Member.builder().id(1L).role(Role.MEMBER).build();
    testTrainer = Trainer.builder().id(2L).role(Role.TRAINER).build();
    memberPrincipal = new UserPrincipal(testMember);
    trainerPrincipal = new UserPrincipal(testTrainer);
    testOffering = PTOffering.builder().id(10L).trainer(testTrainer).build();
    testApplication = PTApplication.builder().id(100L).member(testMember).offering(testOffering).status(PTApplicationStatus.PENDING).build();
  }


  @Nested
  @DisplayName("PT 신청 수락 (acceptApplication)")
  class AcceptApplicationTest {

    @Test
    @DisplayName("성공: 트레이너가 자신에게 온 PT 신청을 수락하고 계약이 생성된다")
    void acceptApplication_success() {
      // given
      given(memberRepository.findById(testTrainer.getId())).willReturn(Optional.of(testTrainer));
      given(ptApplicationRepository.findById(testApplication.getId())).willReturn(Optional.of(testApplication));

      // when
      ptApplicationService.acceptApplication(testApplication.getId(), trainerPrincipal);

      // then
      assertThat(testApplication.getStatus()).isEqualTo(PTApplicationStatus.APPROVED);
      then(ptContractService).should().createContractFromApplication(testApplication);
    }

    @Test
    @DisplayName("실패: 다른 트레이너가 수락을 시도하면 AccessDeniedException이 발생한다")
    void acceptApplication_fail_whenNotCorrectTrainer() {
      // given
      Trainer anotherTrainer = Trainer.builder().id(99L).role(Role.TRAINER).build();
      UserPrincipal anotherTrainerPrincipal = new UserPrincipal(anotherTrainer);
      given(memberRepository.findById(anotherTrainer.getId())).willReturn(Optional.of(anotherTrainer));
      given(ptApplicationRepository.findById(testApplication.getId())).willReturn(Optional.of(testApplication));

      // when & then
      assertThatThrownBy(() -> ptApplicationService.acceptApplication(testApplication.getId(), anotherTrainerPrincipal))
          .isInstanceOf(AccessDeniedException.class)
          .hasMessage("자신에게 온 PT 신청이 아닙니다.");
      then(ptContractService).should(never()).createContractFromApplication(any());
    }

    @Test
    @DisplayName("실패: 일반 회원이 수락을 시도하면 AccessDeniedException이 발생한다")
    void acceptApplication_fail_whenUserIsMember() {
      // given
      given(memberRepository.findById(testMember.getId())).willReturn(Optional.of(testMember));
      given(ptApplicationRepository.findById(testApplication.getId())).willReturn(Optional.of(testApplication));

      // when & then
      assertThatThrownBy(() -> ptApplicationService.acceptApplication(testApplication.getId(), memberPrincipal))
          .isInstanceOf(AccessDeniedException.class)
          .hasMessage("트레이너만 이용할 수 있는 서비스입니다.");
    }
  }

  @Nested
  @DisplayName("PT 신청 취소 (cancelApplication)")
  class CancelApplicationTest {

    @Test
    @DisplayName("성공: 회원이 본인의 PT 신청을 취소한다")
    void cancelApplication_success() {
      // given
      given(memberRepository.findById(testMember.getId())).willReturn(Optional.of(testMember));
      given(ptApplicationRepository.findById(testApplication.getId())).willReturn(Optional.of(testApplication));

      // when
      ptApplicationService.cancelApplication(testApplication.getId(), memberPrincipal);

      // then
      assertThat(testApplication.getStatus()).isEqualTo(PTApplicationStatus.CANCELLED);
    }

    @Test
    @DisplayName("실패: 다른 회원이 PT 신청을 취소하려 하면 AccessDeniedException이 발생한다")
    void cancelApplication_fail_whenNotOwner() {
      // given
      Member anotherMember = Member.builder().id(99L).role(Role.MEMBER).build();
      UserPrincipal anotherMemberPrincipal = new UserPrincipal(anotherMember);
      given(memberRepository.findById(anotherMember.getId())).willReturn(Optional.of(anotherMember));
      given(ptApplicationRepository.findById(testApplication.getId())).willReturn(Optional.of(testApplication));

      // when & then
      assertThatThrownBy(() -> ptApplicationService.cancelApplication(testApplication.getId(), anotherMemberPrincipal))
          .isInstanceOf(AccessDeniedException.class)
          .hasMessage("자신의 PT 신청만 취소할 수 있습니다.");
    }
  }
}