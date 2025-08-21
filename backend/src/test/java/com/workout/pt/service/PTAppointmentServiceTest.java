package com.workout.pt.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.pt.domain.contract.PTAppointment;
import com.workout.pt.domain.contract.PTAppointmentStatus;
import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import com.workout.pt.dto.request.AppointmentRequest;
import com.workout.pt.repository.PTAppointmentRepository;
import com.workout.pt.repository.PTContractRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("PTAppointmentService 단위 테스트")
class PTAppointmentServiceTest {

  @InjectMocks
  private PTAppointmentService ptAppointmentService;

  @Mock
  private PTAppointmentRepository ptAppointmentRepository;
  @Mock
  private PTContractRepository ptContractRepository;
  @Mock
  private PTContractService ptContractService;

  private Member testMember;
  private Trainer testTrainer;
  private UserPrincipal memberPrincipal;
  private UserPrincipal trainerPrincipal;
  private PTContract activeContract;
  private AppointmentRequest appointmentRequest;
  private LocalDateTime startTime = LocalDateTime.of(2025, 8, 22, 10, 0);
  private LocalDateTime endTime = LocalDateTime.of(2025, 8, 22, 11, 0);

  @BeforeEach
  void setUp() {
    testMember = Member.builder().id(1L).role(Role.MEMBER).name("테스트회원").build();
    testTrainer = Trainer.builder().id(2L).role(Role.TRAINER).name("테스트트레이너").build();
    memberPrincipal = new UserPrincipal(testMember);
    trainerPrincipal = new UserPrincipal(testTrainer);

    activeContract = PTContract.builder()
        .id(100L).member(testMember).trainer(testTrainer)
        .status(PTContractStatus.ACTIVE).remainingSessions(10L)
        .build();

    appointmentRequest = new AppointmentRequest(startTime, endTime, null, null, null, activeContract.getId());
  }

  @Nested
  @DisplayName("PT 스케줄 생성 (create)")
  class CreateTest {

    @Test
    @DisplayName("성공: 트레이너가 스케줄을 생성한다")
    void create_success() {
      // given
      given(ptContractRepository.findById(activeContract.getId())).willReturn(Optional.of(activeContract));
      given(ptAppointmentRepository.existsOverlappingAppointment(any(), any(), any())).willReturn(false);

      given(ptAppointmentRepository.save(any(PTAppointment.class)))
          .willAnswer(invocation -> {
            PTAppointment appointmentToSave = invocation.getArgument(0);
            return appointmentToSave;
          });

      // when
      ptAppointmentService.create(trainerPrincipal, appointmentRequest);

      // then
      then(ptAppointmentRepository).should().save(any(PTAppointment.class));
    }

    @Test
    @DisplayName("실패: 트레이너가 아닌 다른 사람이 스케줄을 생성하려 하면 AccessDeniedException이 발생한다")
    void create_fail_whenNotTrainerOfContract() {
      // given
      Trainer anotherTrainer = Trainer.builder().id(99L).role(Role.TRAINER).build();
      UserPrincipal anotherTrainerPrincipal = new UserPrincipal(anotherTrainer);
      given(ptContractRepository.findById(activeContract.getId())).willReturn(Optional.of(activeContract));

      // when & then
      assertThatThrownBy(() -> ptAppointmentService.create(anotherTrainerPrincipal, appointmentRequest))
          .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("실패: 남은 세션이 없으면 IllegalStateException이 발생한다")
    void create_fail_whenNoSessionLeft() {
      // given
      activeContract.setRemainingSessions(0L);
      given(ptContractRepository.findById(activeContract.getId())).willReturn(Optional.of(activeContract));

      // when & then
      assertThatThrownBy(() -> ptAppointmentService.create(trainerPrincipal, appointmentRequest))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("남은 PT 세션이 없습니다.");
    }

    @Test
    @DisplayName("실패: 요청된 시간에 이미 다른 예약이 있으면 IllegalStateException이 발생한다")
    void create_fail_whenScheduleOverlaps() {
      // given
      given(ptContractRepository.findById(activeContract.getId())).willReturn(Optional.of(activeContract));
      given(ptAppointmentRepository.existsOverlappingAppointment(testTrainer.getId(), startTime, endTime)).willReturn(true);

      // when & then
      assertThatThrownBy(() -> ptAppointmentService.create(trainerPrincipal, appointmentRequest))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("해당 시간에 이미 다른 예약이 존재합니다.");
    }
  }

  @Nested
  @DisplayName("PT 스케줄 상태 변경 (updateStatus)")
  class UpdateStatusTest {
    @Test
    @DisplayName("성공: 트레이너가 스케줄을 COMPLETED로 변경하면 세션이 차감된다")
    void updateStatus_toCompleted_success() {
      // given
      PTAppointment appointment = PTAppointment.builder().id(1L).contract(activeContract).build();
      given(ptAppointmentRepository.findById(appointment.getId())).willReturn(Optional.of(appointment));

      // when
      ptAppointmentService.updateStatus(trainerPrincipal, appointment.getId(), PTAppointmentStatus.COMPLETED);

      // then
      assertThat(appointment.getStatus()).isEqualTo(PTAppointmentStatus.COMPLETED);
      then(ptContractService).should().deductSession(activeContract.getId());
    }

    @Test
    @DisplayName("성공: 트레이너가 스케줄을 CANCELLED로 변경하면 세션이 차감되지 않는다")
    void updateStatus_toCancelled_success() {
      // given
      PTAppointment appointment = PTAppointment.builder().id(1L).contract(activeContract).build();
      given(ptAppointmentRepository.findById(appointment.getId())).willReturn(Optional.of(appointment));

      // when
      ptAppointmentService.updateStatus(trainerPrincipal, appointment.getId(), PTAppointmentStatus.CANCELLED);

      // then
      assertThat(appointment.getStatus()).isEqualTo(PTAppointmentStatus.CANCELLED);
      then(ptContractService).should(never()).deductSession(any());
    }
  }
}