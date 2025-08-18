package com.workout.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

  @InjectMocks
  private AuthService authService;

  @Mock
  private MemberRepository memberRepository; // Mock MemberRepository instead of MemberService

  @Mock
  private PasswordEncoder passwordEncoder; // Mock PasswordEncoder

  @Mock
  private SecurityContextRepository securityContextRepository;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  private Member testMember;

  @BeforeEach
  void setUp() {
    testMember = Member.builder()
        .id(1L)
        .email("test@example.com")
        .name("테스트유저")
        .password("encoded_password") // Set a dummy encoded password
        .role(Role.MEMBER)
        .build();
  }

  @Nested
  @DisplayName("로그인 성공 테스트")
  class LoginSuccessTest {

    @Test
    @DisplayName("성공: 올바른 아이디와 비밀번호 입력 시, 인증된 SecurityContext를 생성하고 세션에 저장한다")
    void login_success() {
      // given (준비)
      String email = "test@example.com";
      String rawPassword = "password123";

      given(memberRepository.findByEmail(email)).willReturn(Optional.of(testMember));

      given(passwordEncoder.matches(rawPassword, testMember.getPassword())).willReturn(true);

      // when (실행)
      Member result = authService.login(email, rawPassword, request, response);

      // then (검증)
      assertThat(result).isEqualTo(testMember);

      then(memberRepository).should().findByEmail(email);
      then(passwordEncoder).should().matches(rawPassword, testMember.getPassword());

      ArgumentCaptor<SecurityContext> contextCaptor = ArgumentCaptor.forClass(
          SecurityContext.class);
      then(securityContextRepository).should()
          .saveContext(contextCaptor.capture(), any(HttpServletRequest.class),
              any(HttpServletResponse.class));

      SecurityContext capturedContext = contextCaptor.getValue();
      Authentication authentication = capturedContext.getAuthentication();

      assertThat(authentication.isAuthenticated()).isTrue();
      assertThat(authentication.getPrincipal()).isInstanceOf(UserPrincipal.class);
      UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
      assertThat(principal.getUserId()).isEqualTo(testMember.getId());
      assertThat(
          principal.getAuthorities().stream().map(Object::toString).findFirst().orElseThrow())
          .isEqualTo("ROLE_MEMBER");
    }
  }

  @Nested
  @DisplayName("로그인 실패 테스트")
  class LoginFailureTest {

    @Test
    @DisplayName("실패: 비밀번호가 틀리면 IllegalArgumentException이 발생한다")
    void login_fail_withInvalidCredentials() {
      // given (준비)
      String email = "test@example.com";
      String wrongPassword = "wrong_password";

      given(memberRepository.findByEmail(email)).willReturn(Optional.of(testMember));

      given(passwordEncoder.matches(wrongPassword, testMember.getPassword())).willReturn(false);

      // when & then (실행 및 검증)
      assertThatThrownBy(() -> authService.login(email, wrongPassword, request, response))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다."); // Message updated for consistency

      then(securityContextRepository).should(never()).saveContext(any(), any(), any());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 이메일이면 IllegalArgumentException이 발생한다")
    void login_fail_withNonExistentEmail() {
      // given
      String nonExistentEmail = "none@example.com";
      String password = "password123";

      given(memberRepository.findByEmail(nonExistentEmail)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> authService.login(nonExistentEmail, password, request, response))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");

      then(passwordEncoder).should(never()).matches(any(), any());
      then(securityContextRepository).should(never()).saveContext(any(), any(), any());
    }
  }
}