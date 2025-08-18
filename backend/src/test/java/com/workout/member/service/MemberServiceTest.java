package com.workout.member.service;

import com.workout.auth.dto.SignupRequest;
import com.workout.gym.domain.Gym;
import com.workout.gym.service.GymService;
import com.workout.member.domain.AccountStatus;
import com.workout.member.domain.Gender;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class MemberServiceTest {

  @InjectMocks
  private MemberService memberService;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private GymService gymService;

  @Mock
  private PasswordEncoder passwordEncoder;

  // 테스트에서 공통적으로 사용할 객체 (Test Fixtures)
  private Member mockUser;
  private Gym mockGym;
  private SignupRequest signupRequest;
  private final String rawPassword = "password123";
  private final String encodedPassword = "encodedPassword";

  @BeforeEach
  void setUp() {
    mockGym = Gym.builder().id(1L).name("테스트 헬스장").build();

    // SignupRequest record의 필드 순서(gymId, email, password, name, gender, role)에 맞게 인자 전달
    signupRequest = new SignupRequest(
        1L,                         // gymId
        "test@example.com",         // email
        rawPassword,                // password
        "테스트유저",                  // name
        Gender.MALE,                // gender
        Role.MEMBER                   // role
    );

    mockUser = Member.builder()
        .id(1L)
        .email(signupRequest.email())
        .password(encodedPassword)
        .name(signupRequest.name())
        .role(signupRequest.role())
        .gym(mockGym)
        .build();
  }

  @Nested
  @DisplayName("사용자 인증 (authenticate) 로직 테스트")
  class AuthenticateTest {

    @Test
    @DisplayName("성공: 이메일과 비밀번호가 일치하면 사용자 정보를 반환한다")
    void authenticate_Success() {
      // given
      given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(mockUser));
      given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);

      // when
      Member authenticatedUser = memberService.authenticate(signupRequest.email(), rawPassword);

      // then
      assertThat(authenticatedUser).isEqualTo(mockUser);
      then(memberRepository).should(times(1)).findByEmail(signupRequest.email());
      then(passwordEncoder).should(times(1)).matches(rawPassword, encodedPassword);
    }

    @Test
    @DisplayName("실패: 가입되지 않은 이메일이면 IllegalArgumentException 예외가 발생한다")
    void authenticate_Failure_UserNotFound() {
      // given
      given(memberRepository.findByEmail(anyString())).willReturn(Optional.empty());

      // when & then
      assertThrows(IllegalArgumentException.class,
          () -> memberService.authenticate(signupRequest.email(), rawPassword));

      // and
      then(passwordEncoder).should(never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("실패: 비밀번호가 일치하지 않으면 IllegalArgumentException 예외가 발생한다")
    void authenticate_Failure_PasswordMismatch() {
      // given
      given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(mockUser));
      given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false);

      // when & then
      assertThrows(IllegalArgumentException.class,
          () -> memberService.authenticate(signupRequest.email(), rawPassword));
    }
  }

  @Nested
  @DisplayName("회원가입 (registerUser) 로직 테스트")
  class RegisterUserTest {

    @Test
    @DisplayName("성공: 모든 정보가 유효하면 암호화된 비밀번호와 함께 사용자를 저장한다")
    void registerUser_Success() {
      // given
      given(gymService.findById(signupRequest.gymId())).willReturn(mockGym);
      // --- 변경점: 아래 라인을 삭제합니다. ---
      // given(memberRepository.existsByName(signupRequest.name())).willReturn(false);
      given(memberRepository.existsByEmail(signupRequest.email())).willReturn(false);
      given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);
      given(memberRepository.save(any(Member.class))).willReturn(mockUser);

      // when
      Member newUser = memberService.registerUser(signupRequest);

      // then
      ArgumentCaptor<Member> userCaptor = ArgumentCaptor.forClass(Member.class);
      then(memberRepository).should(times(1)).save(userCaptor.capture());
      Member savedUser = userCaptor.getValue();

      // and
      assertThat(newUser).isEqualTo(mockUser);
      assertThat(savedUser.getGym()).isEqualTo(mockGym);
      assertThat(savedUser.getName()).isEqualTo(signupRequest.name());
      assertThat(savedUser.getEmail()).isEqualTo(signupRequest.email());
      assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
      assertThat(savedUser.getRole()).isEqualTo(signupRequest.role());
      assertThat(savedUser.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 Gym ID이면 EntityNotFoundException 예외가 발생한다")
    void registerUser_Failure_InvalidGym() {
      // given
      given(gymService.findById(signupRequest.gymId()))
          .willThrow(new EntityNotFoundException("존재하지 않는 헬스장입니다."));

      // when & then
      assertThrows(EntityNotFoundException.class,
          () -> memberService.registerUser(signupRequest));

      // and
      then(memberRepository).should(never()).existsByName(anyString());
      then(memberRepository).should(never()).existsByEmail(anyString());
      then(memberRepository).should(never()).save(any(Member.class));
    }

  }
}