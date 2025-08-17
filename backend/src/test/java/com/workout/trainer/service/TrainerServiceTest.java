package com.workout.trainer.service;

import com.workout.auth.dto.SignupRequest;
import com.workout.gym.domain.Gym;
import com.workout.gym.service.GymService;
import com.workout.member.domain.Gender;
import com.workout.member.domain.Role;
import com.workout.member.service.MemberService;
import com.workout.trainer.domain.*;
import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.repository.*;
import jakarta.persistence.EntityNotFoundException;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerService 단위 테스트")
class TrainerServiceTest {

  @InjectMocks
  private TrainerService trainerService;

  // Service의 모든 의존성을 Mock으로 선언
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

  // 테스트에서 공통적으로 사용할 Test Fixture
  private Trainer mockTrainer;
  private Gym mockGym;

  @BeforeEach
  void setUp() {
    mockGym = Gym.builder().id(1L).name("테스트 헬스장").build();
    mockTrainer = Trainer.builder()
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
    @DisplayName("성공: 트레이너 ID로 프로필 전체 정보를 정상적으로 조회한다")
    void getProfile_Success() {
      // given
      given(trainerRepository.findById(anyLong())).willReturn(Optional.of(mockTrainer));
      given(awardRepository.findAllByTrainerId(anyLong())).willReturn(List.of(Award.builder().build()));
      given(certificationRepository.findAllByTrainerId(anyLong())).willReturn(List.of(Certification.builder().build()));
      given(educationRepository.findAllByTrainerId(anyLong())).willReturn(List.of(Education.builder().build()));
      given(workexperiencesRepository.findAllByTrainerId(anyLong())).willReturn(List.of(Workexperience.builder().build()));
      given(trainerSpecialtyRepository.findSpecialtiesByTrainerId(anyLong())).willReturn(Set.of(Specialty.builder().name("재활").build()));

      // when
      ProfileResponseDto profile = trainerService.getProfile(mockTrainer.getId());

      // then
      assertThat(profile).isNotNull();
      assertThat(profile.trainerId()).isEqualTo(mockTrainer.getId());
      assertThat(profile.awards()).hasSize(1);
      assertThat(profile.certifications()).hasSize(1);
      assertThat(profile.educations()).hasSize(1);
      assertThat(profile.workExperiences()).hasSize(1);
      // --- 변경된 부분 ---
      assertThat(profile.specialties()).contains("재활");

      // verify
      then(trainerRepository).should().findById(mockTrainer.getId());
      then(awardRepository).should().findAllByTrainerId(mockTrainer.getId());
      // ... 다른 repository들도 모두 호출되었는지 검증 가능
    }

    @Test
    @DisplayName("실패: 존재하지 않는 트레이너 ID로 조회 시 EntityNotFoundException 예외가 발생한다")
    void getProfile_Fail_TrainerNotFound() {
      // given
      given(trainerRepository.findById(anyLong())).willReturn(Optional.empty());

      // when & then
      assertThrows(EntityNotFoundException.class,
          () -> trainerService.getProfile(999L));

      // verify
      then(awardRepository).should(never()).findAllByTrainerId(anyLong());
    }
  }

  @Nested
  @DisplayName("프로필 생성 (createProfile) 테스트")
  class CreateProfileTest {
    @Test
    @DisplayName("성공: 유효한 정보로 프로필을 생성한다")
    void createProfile_Success() {
      // given
      ProfileCreateDto createDto = createMockProfileDto();
      given(trainerRepository.findById(mockTrainer.getId())).willReturn(Optional.of(mockTrainer));

      // when
      trainerService.createProfile(mockTrainer.getId(), createDto);

      // then
      // ArgumentCaptor를 사용하여 saveAll에 전달된 인자를 캡처하고 검증
      ArgumentCaptor<List<Award>> awardCaptor = ArgumentCaptor.forClass(List.class);
      then(awardRepository).should().saveAll(awardCaptor.capture());
      assertThat(awardCaptor.getValue()).hasSize(1);
      assertThat(awardCaptor.getValue().get(0).getAwardName()).isEqualTo("올림피아");

      ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
      then(trainerRepository).should().save(trainerCaptor.capture());
      assertThat(trainerCaptor.getValue().getIntroduction()).isEqualTo("테스트 소개글");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 트레이너 ID로 생성 시도 시 EntityNotFoundException 예외가 발생한다")
    void createProfile_Fail_TrainerNotFound() {
      // given
      ProfileCreateDto createDto = createMockProfileDto();
      given(trainerRepository.findById(anyLong())).willReturn(Optional.empty());

      // when & then
      assertThrows(EntityNotFoundException.class,
          () -> trainerService.createProfile(999L, createDto));

      // verify
      then(awardRepository).should(never()).saveAll(any());
    }
  }

  @Nested
  @DisplayName("프로필 수정 (updateProfile) 테스트")
  class UpdateProfileTest {
    @Test
    @DisplayName("성공: 기존 프로필 정보를 모두 삭제 후 새로운 정보로 덮어쓴다")
    void updateProfile_Success() {
      // given
      ProfileCreateDto updateDto = createMockProfileDto();
      given(trainerRepository.findById(mockTrainer.getId())).willReturn(Optional.of(mockTrainer));

      // when
      trainerService.updateProfile(mockTrainer.getId(), updateDto);

      // then
      // InOrder를 사용하여 메서드 호출 순서 검증 (삭제 -> 저장)
      InOrder inOrder = inOrder(awardRepository, certificationRepository, trainerRepository);
      then(awardRepository).should(inOrder).deleteAllByTrainerId(mockTrainer.getId());
      then(trainerRepository).should(inOrder).save(any(Trainer.class));
      then(awardRepository).should(inOrder).saveAll(any());
    }
  }

  @Nested
  @DisplayName("트레이너 회원가입 (registerTrainer) 테스트")
  class RegisterTrainerTest {

    @Test
    @DisplayName("성공: 유효한 정보로 트레이너 회원가입을 성공한다")
    void registerTrainer_Success() {
      // given
      SignupRequest request = new SignupRequest(
          mockGym.getId(), "newtrainer@test.com", "password123",
          "새로운트레이너", Gender.MALE, Role.TRAINER
      );
      String encodedPassword = "encodedPassword";

      given(gymService.findById(mockGym.getId())).willReturn(mockGym);
      willDoNothing().given(memberService).ensureEmailIsUnique(request.email());
      given(passwordEncoder.encode(request.password())).willReturn(encodedPassword);

      // save 메서드가 호출될 때 반환될 Trainer 객체 설정
      given(trainerRepository.save(any(Trainer.class))).willAnswer(invocation -> invocation.getArgument(0));

      // when
      Trainer newTrainer = trainerService.registerTrainer(request);

      // then
      ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
      then(trainerRepository).should().save(trainerCaptor.capture());
      Trainer savedTrainer = trainerCaptor.getValue();

      assertThat(newTrainer.getEmail()).isEqualTo(request.email());
      assertThat(savedTrainer.getPassword()).isEqualTo(encodedPassword);
      assertThat(savedTrainer.getGym()).isEqualTo(mockGym);
      assertThat(savedTrainer.getRole()).isEqualTo(Role.TRAINER);
    }

    @Test
    @DisplayName("실패: 이미 존재하는 이메일로 가입 시도 시 IllegalArgumentException 예외가 발생한다")
    void registerTrainer_Fail_DuplicateEmail() {
      // given
      SignupRequest request = new SignupRequest(
          mockGym.getId(), "trainer@test.com", "password123",
          "트레이너", Gender.FEMALE, Role.TRAINER
      );
      given(gymService.findById(mockGym.getId())).willReturn(mockGym);
      willThrow(new IllegalArgumentException("이미 존재하는 이메일입니다."))
          .given(memberService).ensureEmailIsUnique(request.email());

      // when & then
      assertThrows(IllegalArgumentException.class,
          () -> trainerService.registerTrainer(request));

      // verify
      then(passwordEncoder).should(never()).encode(anyString());
      then(trainerRepository).should(never()).save(any(Trainer.class));
    }
  }


  // 테스트용 DTO 생성 헬퍼 메서드
  private ProfileCreateDto createMockProfileDto() {
    return new ProfileCreateDto(
        "테스트 소개글",
        List.of(new ProfileCreateDto.AwardDto("올림피아", null, "라스베가스")),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        Set.of("재활", "교정")
    );
  }
}