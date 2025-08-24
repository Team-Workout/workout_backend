package com.workout.pt.service.session;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.pt.domain.contract.PTAppointment;
import com.workout.pt.domain.contract.PTAppointmentStatus;
import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.session.PTSession;
import com.workout.pt.dto.request.PTSessionCreateRequest;
import com.workout.pt.repository.PTAppointmentRepository;
import com.workout.pt.repository.PTSessionRepository;
import com.workout.pt.service.contract.PTContractService;
import com.workout.trainer.domain.Trainer;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.dto.log.WorkoutLogCreateRequest;
import com.workout.workout.service.WorkoutLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("PTSessionService 단위 테스트")
class PTSessionServiceTest {

  @InjectMocks
  private PTSessionService ptSessionService;

  @Mock private PTSessionRepository ptSessionRepository;
  @Mock private PTAppointmentRepository ptAppointmentRepository;
  @Mock private WorkoutLogService workoutLogService;
  @Mock private PTContractService ptContractService;

  private Member testMember;
  private Trainer testTrainer;
  private UserPrincipal trainerPrincipal;
  private PTAppointment scheduledAppointment;
  private PTContract testContract;
  private WorkoutLogCreateRequest workoutLogCreateRequest;

  @BeforeEach
  void setUp() {
    testMember = Member.builder().id(1L).email("member@test.com").password("p").role(Role.MEMBER).build();
    testTrainer = Trainer.builder().id(2L).email("trainer@test.com").password("p").role(Role.TRAINER).build();
    trainerPrincipal = new UserPrincipal(testTrainer);

    testContract = PTContract.builder().id(10L).member(testMember).trainer(testTrainer).build();
    scheduledAppointment = PTAppointment.builder().id(201L).contract(testContract).status(PTAppointmentStatus.SCHEDULED).build();
    workoutLogCreateRequest = new WorkoutLogCreateRequest(null, null, null);
  }

  @Nested
  @DisplayName("PT 세션 및 운동일지 생성 (createPTSessionAndWorkoutLog)")
  class CreatePTSessionAndWorkoutLog {

    @Test
    @DisplayName("성공: 트레이너가 요청 시 모든 로직이 순서대로 실행된다")
    void create_success() {
      PTSessionCreateRequest request = new PTSessionCreateRequest(scheduledAppointment.getId(), workoutLogCreateRequest);
      WorkoutLog createdWorkoutLog = WorkoutLog.builder().id(101L).build();
      PTSession savedSession = PTSession.builder().id(999L).build();

      given(ptAppointmentRepository.findById(request.appointmentId())).willReturn(Optional.of(scheduledAppointment));
      given(workoutLogService.createWorkoutLog(any(), any(UserPrincipal.class))).willReturn(createdWorkoutLog);
      given(ptSessionRepository.save(any(PTSession.class))).willReturn(savedSession);

      ptSessionService.createPTSessionAndWorkoutLog(request, trainerPrincipal);

      then(workoutLogService).should().createWorkoutLog(any(WorkoutLogCreateRequest.class), any(UserPrincipal.class));
      then(ptSessionRepository).should().save(any(PTSession.class));
      assertThat(scheduledAppointment.getStatus()).isEqualTo(PTAppointmentStatus.COMPLETED);
      then(ptContractService).should().deductSession(testContract.getId());
    }

    @Test
    @DisplayName("실패: 수업이 SCHEDULED 상태가 아니면 IllegalStateException이 발생한다")
    void create_fail_whenNotScheduled() {
      scheduledAppointment.setStatus(PTAppointmentStatus.COMPLETED);
      PTSessionCreateRequest request = new PTSessionCreateRequest(scheduledAppointment.getId(), workoutLogCreateRequest);
      given(ptAppointmentRepository.findById(request.appointmentId())).willReturn(Optional.of(scheduledAppointment));

      assertThatThrownBy(() -> ptSessionService.createPTSessionAndWorkoutLog(request, trainerPrincipal))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("예약(SCHEDULED) 상태인 수업에 대해서만 세션을 생성할 수 있습니다.");
    }
  }
}