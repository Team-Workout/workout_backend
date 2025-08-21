package com.workout.pt.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import com.workout.pt.repository.PTContractRepository;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("PTContractService 단위 테스트")
class PTContractServiceTest {

  @InjectMocks
  private PTContractService ptContractService;

  @Mock
  private PTContractRepository ptContractRepository;

  private Member testMember;
  private Trainer testTrainer;
  private UserPrincipal memberPrincipal;
  private UserPrincipal trainerPrincipal;
  private PTContract activeContract;

  @BeforeEach
  void setUp() {
    testMember = Member.builder().id(1L).role(Role.MEMBER).name("테스트회원").build();
    testTrainer = Trainer.builder().id(2L).role(Role.TRAINER).name("테스트트레이너").build();
    memberPrincipal = new UserPrincipal(testMember);
    trainerPrincipal = new UserPrincipal(testTrainer);

    activeContract = PTContract.builder()
        .id(100L)
        .member(testMember)
        .trainer(testTrainer)
        .status(PTContractStatus.ACTIVE)
        .remainingSessions(10L)
        .build();
  }

  @Nested
  @DisplayName("세션 차감 (deductSession)")
  class DeductSessionTest {

    @Test
    @DisplayName("성공: 남은 세션이 1 차감된다")
    void deductSession_success() {
      // given
      given(ptContractRepository.findById(activeContract.getId())).willReturn(Optional.of(activeContract));

      // when
      ptContractService.deductSession(activeContract.getId());

      // then
      assertThat(activeContract.getRemainingSessions()).isEqualTo(9L);
      assertThat(activeContract.getStatus()).isEqualTo(PTContractStatus.ACTIVE);
    }

    @Test
    @DisplayName("성공: 마지막 세션 차감 시 상태가 COMPLETED로 변경된다")
    void deductSession_success_lastSession() {
      // given
      activeContract.setRemainingSessions(1L);
      given(ptContractRepository.findById(activeContract.getId())).willReturn(Optional.of(activeContract));

      // when
      ptContractService.deductSession(activeContract.getId());

      // then
      assertThat(activeContract.getRemainingSessions()).isZero();
      assertThat(activeContract.getStatus()).isEqualTo(PTContractStatus.COMPLETED);
    }

    @Test
    @DisplayName("실패: 남은 세션이 0일 경우 IllegalStateException이 발생한다")
    void deductSession_fail_whenNoSessionLeft() {
      // given
      activeContract.setRemainingSessions(0L);
      given(ptContractRepository.findById(activeContract.getId())).willReturn(Optional.of(activeContract));

      // when & then
      assertThatThrownBy(() -> ptContractService.deductSession(activeContract.getId()))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 계약 ID일 경우 EntityNotFoundException이 발생한다")
    void deductSession_fail_whenContractNotFound() {
      // given
      given(ptContractRepository.findById(999L)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> ptContractService.deductSession(999L))
          .isInstanceOf(EntityNotFoundException.class);
    }
  }


  @Nested
  @DisplayName("계약 취소 (cancelContractByMember)")
  class CancelContractByMemberTest {

    @Test
    @DisplayName("성공: 회원이 자신의 ACTIVE 상태인 계약을 취소한다")
    void cancelContractByMember_success() {
      // given
      given(ptContractRepository.findById(activeContract.getId())).willReturn(Optional.of(activeContract));

      // when
      ptContractService.cancelContractByMember(memberPrincipal, activeContract.getId());

      // then
      assertThat(activeContract.getStatus()).isEqualTo(PTContractStatus.CANCELLED);
    }

    @Test
    @DisplayName("실패: 자신의 계약이 아닌 경우 AccessDeniedException이 발생한다")
    void cancelContractByMember_fail_whenNotOwner() {
      // given
      Member anotherMember = Member.builder().id(99L).role(Role.MEMBER).build();
      UserPrincipal anotherPrincipal = new UserPrincipal(anotherMember);
      given(ptContractRepository.findById(activeContract.getId())).willReturn(Optional.of(activeContract));

      // when & then
      assertThatThrownBy(() -> ptContractService.cancelContractByMember(anotherPrincipal, activeContract.getId()))
          .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("실패: 계약이 ACTIVE 상태가 아닌 경우 IllegalStateException이 발생한다")
    void cancelContractByMember_fail_whenStatusNotActive() {
      // given
      activeContract.setStatus(PTContractStatus.COMPLETED);
      given(ptContractRepository.findById(activeContract.getId())).willReturn(Optional.of(activeContract));

      // when & then
      assertThatThrownBy(() -> ptContractService.cancelContractByMember(memberPrincipal, activeContract.getId()))
          .isInstanceOf(IllegalStateException.class);
    }
  }

  @Nested
  @DisplayName("나의 계약 목록 조회 (getMyContracts)")
  class GetMyContractsTest {

    @Test
    @DisplayName("성공: 회원으로 요청 시 findAllByMemberId가 호출된다")
    void getMyContracts_asMember() {
      // given
      PageRequest pageable = PageRequest.of(0, 10);
      Page<PTContract> contractPage = new PageImpl<>(List.of(activeContract));
      given(ptContractRepository.findAllByMemberId(testMember.getId(), pageable)).willReturn(contractPage);

      // when
      ptContractService.getMyContracts(memberPrincipal, pageable);

      // then
      then(ptContractRepository).should().findAllByMemberId(testMember.getId(), pageable);
    }

    @Test
    @DisplayName("성공: 트레이너로 요청 시 findAllByTrainerId가 호출된다")
    void getMyContracts_asTrainer() {
      // given
      PageRequest pageable = PageRequest.of(0, 10);
      Page<PTContract> contractPage = new PageImpl<>(List.of(activeContract));
      given(ptContractRepository.findAllByTrainerId(testTrainer.getId(), pageable)).willReturn(contractPage);

      // when
      ptContractService.getMyContracts(trainerPrincipal, pageable);

      // then
      then(ptContractRepository).should().findAllByTrainerId(testTrainer.getId(), pageable);
    }
  }
}