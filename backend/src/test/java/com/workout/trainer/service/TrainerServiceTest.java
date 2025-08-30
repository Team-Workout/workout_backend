package com.workout.trainer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.inOrder;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

import com.workout.auth.dto.SignupRequest;
import com.workout.gym.domain.Gym;
import com.workout.gym.service.GymService;
import com.workout.member.domain.Gender;
import com.workout.member.domain.Role;
import com.workout.member.service.MemberService;
import com.workout.trainer.domain.Award;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.repository.AwardRepository;
import com.workout.trainer.repository.CertificationRepository;
import com.workout.trainer.repository.EducationRepository;
import com.workout.trainer.repository.SpecialtyRepository;
import com.workout.trainer.repository.TrainerRepository;
import com.workout.trainer.repository.TrainerSpecialtyRepository;
import com.workout.trainer.repository.WorkexperiencesRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerService 단위 테스트")
class TrainerServiceTest {

  @InjectMocks
  private TrainerService trainerService;

  @Mock
  private TrainerRepository trainerRepository;
  @Mock
  private AwardRepository awardRepository;
  @Mock
  private CertificationRepository certificationRepository;
  @Mock
  private EducationRepository educationRepository;
  @Mock
  private WorkexperiencesRepository workexperiencesRepository;
  @Mock
  private SpecialtyRepository specialtyRepository;
  @Mock
  private TrainerSpecialtyRepository trainerSpecialtyRepository;
  @Mock
  private GymService gymService;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private MemberService memberService;

  private Trainer testTrainer;
  private Gym mockGym;

  @BeforeEach
  void setUp() {
    mockGym = Gym.builder().id(1L).name("테스트 헬스장").build();
    // [수정] 혼동을 피하기 위해 변수명을 mockTrainer -> testTrainer로 변경
    testTrainer = Trainer.builder()
        .id(1L)
        .name("테스트 트레이너")
        .email("trainer@test.com")
        .gym(mockGym)
        .build();
  }

  @Nested
  @DisplayName("프로필 조회 (getProfile) 테스트")
  class GetProfileTest {

    @Test
    @DisplayName("성공: 존재하는 트레이너의 프로필 정보를 올바르게 반환한다")
    void getProfile_Success() {
      // given
      given(trainerRepository.findById(testTrainer.getId())).willReturn(Optional.of(testTrainer));
      given(awardRepository.findAllByTrainerId(testTrainer.getId())).willReturn(
          Collections.emptyList());
      given(certificationRepository.findAllByTrainerId(testTrainer.getId())).willReturn(
          Collections.emptyList());
      given(educationRepository.findAllByTrainerId(testTrainer.getId())).willReturn(
          Collections.emptyList());
      given(workexperiencesRepository.findAllByTrainerId(testTrainer.getId())).willReturn(
          Collections.emptyList());
      given(trainerSpecialtyRepository.findSpecialtiesByTrainerId(testTrainer.getId())).willReturn(
          Collections.emptySet());

      // when
      ProfileResponseDto profile = trainerService.getProfile(testTrainer.getId());

      // then
      assertThat(profile).isNotNull();
      assertThat(profile.trainerId()).isEqualTo(testTrainer.getId());
      assertThat(profile.name()).isEqualTo(testTrainer.getName());
      then(trainerRepository).should().findById(testTrainer.getId());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 트레이너 ID로 조회 시 EntityNotFoundException 예외가 발생한다")
    void getProfile_Fail_TrainerNotFound() {
      // given
      given(trainerRepository.findById(anyLong())).willReturn(Optional.empty());

      // when & then
      assertThrows(EntityNotFoundException.class, () -> trainerService.getProfile(999L));
    }
  }

  @Nested
  @DisplayName("프로필 수정 (updateProfile) 테스트")
  class UpdateProfileTest {

    @Test
    @DisplayName("성공: 변경사항(생성, 수정, 삭제)을 정확히 감지하여 DB에 반영한다")
    void updateProfile_DetectsChanges_Success() {
      // given
      Award awardToUpdate = Award.builder().trainer(testTrainer).awardName("옛날 수상기록").build();
      ReflectionTestUtils.setField(awardToUpdate, "id", 101L);

      Award awardToDelete = Award.builder().trainer(testTrainer).awardName("삭제될 수상기록").build();
      ReflectionTestUtils.setField(awardToDelete, "id", 102L);

      given(awardRepository.findAllByTrainerId(testTrainer.getId())).willReturn(
          new ArrayList<>(List.of(awardToUpdate, awardToDelete)));
      given(trainerRepository.findById(testTrainer.getId())).willReturn(Optional.of(testTrainer));

      ProfileCreateDto.AwardDto updatedDto = new ProfileCreateDto.AwardDto(101L, "새로운 수상기록", null,
          null);
      ProfileCreateDto.AwardDto newDto = new ProfileCreateDto.AwardDto(null, "신규 수상!", null, null);
      ProfileCreateDto updateRequestDto = new ProfileCreateDto(
          "새로운 소개글", List.of(updatedDto, newDto),
          Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
          Collections.emptySet()
      );

      // when
      trainerService.updateProfile(testTrainer.getId(), updateRequestDto);

      // then
      assertThat(testTrainer.getIntroduction()).isEqualTo("새로운 소개글");

      ArgumentCaptor<Award> newAwardCaptor = ArgumentCaptor.forClass(Award.class);
      then(awardRepository).should().save(newAwardCaptor.capture());
      assertThat(newAwardCaptor.getValue().getAwardName()).isEqualTo("신규 수상!");

      // [오류 수정] ArgumentCaptor의 타입을 List에서 Collection으로 변경하여 실제 타입과 일치시킵니다.
      ArgumentCaptor<Collection<Award>> deletedAwardsCaptor = ArgumentCaptor.forClass(
          Collection.class);
      then(awardRepository).should().deleteAll(deletedAwardsCaptor.capture());

      // 검증 로직
      Collection<Award> capturedAwards = deletedAwardsCaptor.getValue();
      assertThat(capturedAwards).hasSize(1);
      assertThat(capturedAwards.iterator().next().getId()).isEqualTo(102L);
    }
  }

  @Nested
  @DisplayName("프로필 삭제 (deleteProfile) 테스트")
  class DeleteProfileTest {

    @Test
    @DisplayName("성공: 하위 엔티티들을 모두 삭제한 후 트레이너를 삭제한다")
    void deleteProfile_Success() {
      // given
      given(trainerRepository.existsById(testTrainer.getId())).willReturn(true);

      // when
      trainerService.deleteProfile(testTrainer.getId());

      // then
      InOrder inOrder = inOrder(trainerSpecialtyRepository, trainerRepository);

      then(awardRepository).should().deleteAllByTrainerId(testTrainer.getId());
      then(certificationRepository).should().deleteAllByTrainerId(testTrainer.getId());
      then(educationRepository).should().deleteAllByTrainerId(testTrainer.getId());
      then(workexperiencesRepository).should().deleteAllByTrainerId(testTrainer.getId());
      then(trainerSpecialtyRepository).should(inOrder).deleteAllByTrainerId(testTrainer.getId());
      then(trainerRepository).should(inOrder).deleteById(testTrainer.getId());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 트레이너 ID로 삭제 시 EntityNotFoundException 예외가 발생한다")
    void deleteProfile_Fail_TrainerNotFound() {
      // given
      given(trainerRepository.existsById(anyLong())).willReturn(false);

      // when & then
      assertThrows(EntityNotFoundException.class, () -> trainerService.deleteProfile(999L));

      // verify
      then(awardRepository).should(never()).deleteAllByTrainerId(anyLong());
      then(trainerRepository).should(never()).deleteById(anyLong());
    }
  }

  @Nested
  @DisplayName("트레이너 등록 (registerTrainer) 테스트")
  class RegisterTrainerTest {

    @Test
    @DisplayName("성공: 유효한 정보로 트레이너를 등록한다")
    void registerTrainer_Success() {
      // given
      SignupRequest signupRequest = new SignupRequest(mockGym.getId(), "new@test.com",
          "password123", "신규트레이너", Gender.MALE, Role.TRAINER);
      given(gymService.findById(mockGym.getId())).willReturn(mockGym);
      willDoNothing().given(memberService).ensureEmailIsUnique(signupRequest.email());
      given(passwordEncoder.encode(signupRequest.password())).willReturn("encodedPassword");

      Trainer newTrainer = signupRequest.toTrainerEntity(mockGym, "encodedPassword");
      given(trainerRepository.save(any(Trainer.class))).willReturn(newTrainer);

      // when
      Trainer registeredTrainer = trainerService.registerTrainer(signupRequest);

      // then
      assertThat(registeredTrainer).isNotNull();
      assertThat(registeredTrainer.getEmail()).isEqualTo(signupRequest.email());
      then(memberService).should().ensureEmailIsUnique(signupRequest.email());
      then(trainerRepository).should().save(any(Trainer.class));
    }
  }
}