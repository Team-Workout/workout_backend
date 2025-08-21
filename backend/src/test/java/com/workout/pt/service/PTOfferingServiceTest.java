package com.workout.pt.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import com.workout.pt.domain.contract.PTOffering;
import com.workout.pt.dto.request.OfferingCreateRequest;
import com.workout.pt.repository.PTOfferingRepository;
import com.workout.pt.service.contract.PTOfferingService;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("PTOfferingService 단위 테스트")
class PTOfferingServiceTest {

  @InjectMocks
  private PTOfferingService ptOfferingService;

  @Mock
  private PTOfferingRepository ptOfferingRepository;

  @Mock
  private MemberRepository memberRepository;

  private Trainer testTrainer;
  private Member testMember;
  private UserPrincipal trainerPrincipal;
  private UserPrincipal memberPrincipal;
  private OfferingCreateRequest createRequest;

  @BeforeEach
  void setUp() {
    testTrainer = Trainer.builder().id(1L).name("테스트 트레이너").role(Role.TRAINER).build();
    testMember = Member.builder().id(2L).name("테스트 회원").role(Role.MEMBER).build();
    trainerPrincipal = new UserPrincipal(testTrainer);
    memberPrincipal = new UserPrincipal(testMember);
    createRequest = new OfferingCreateRequest("PT 10회", "설명", 500000L, 10L, 3L);
  }

  @Nested
  @DisplayName("PT 상품 등록 (register)")
  class RegisterTest {

    @Test
    @DisplayName("성공: 트레이너가 PT 상품을 등록한다")
    void register_success() {
      // given
      given(memberRepository.findById(testTrainer.getId())).willReturn(Optional.of(testTrainer));

      // when
      ptOfferingService.register(createRequest, trainerPrincipal);

      // then
      then(ptOfferingRepository).should().save(any(PTOffering.class));
    }

    @Test
    @DisplayName("실패: 일반 회원이 PT 상품 등록을 시도하면 AccessDeniedException이 발생한다")
    void register_fail_whenUserIsMember() {
      // given
      given(memberRepository.findById(testMember.getId())).willReturn(Optional.of(testMember));

      // when & then
      assertThatThrownBy(() -> ptOfferingService.register(createRequest, memberPrincipal))
          .isInstanceOf(AccessDeniedException.class)
          .hasMessage("트레이너만 PT 상품을 등록할 수 있습니다.");

      then(ptOfferingRepository).should(never()).save(any());
    }
  }

  @Nested
  @DisplayName("PT 상품 삭제 (delete)")
  class DeleteTest {

    @Test
    @DisplayName("실패: 자신의 PT 상품이 아닌 경우 AccessDeniedException이 발생한다")
    void delete_fail_whenNotOwner() {
      // given
      Trainer anotherTrainer = Trainer.builder().id(99L).role(Role.TRAINER).build();
      PTOffering offering = PTOffering.builder().id(100L).trainer(anotherTrainer).build();
      given(memberRepository.findById(testTrainer.getId())).willReturn(Optional.of(testTrainer));
      given(ptOfferingRepository.findById(offering.getId())).willReturn(Optional.of(offering));

      // when & then
      assertThatThrownBy(() -> ptOfferingService.delete(offering.getId(), trainerPrincipal))
          .isInstanceOf(AccessDeniedException.class)
          .hasMessage("오퍼를 지울 권한이 없습니다.");
    }
  }
}