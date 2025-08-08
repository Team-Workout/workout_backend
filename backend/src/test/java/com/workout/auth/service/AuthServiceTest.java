package com.workout.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.workout.auth.domain.SessionConst;
import com.workout.auth.domain.UserSessionDto;
import com.workout.user.domain.User;
import com.workout.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

  @Mock
  private UserService userService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpSession session;

  @InjectMocks
  private AuthService authService;

  // --- 1. @Nested를 사용하여 테스트들을 논리적인 그룹으로 묶습니다. ---
  @Nested
  @DisplayName("로그인 테스트")
  class LoginTest {

    @Test
    @DisplayName("성공: 올바른 자격 증명으로 로그인에 성공한다")
    void login_success() {
      // given (주어진 상황)
      String email = "test@example.com";
      String password = "password123";
      User mockUser = User.builder().id(1L).email(email).name("테스트유저").build();

      // --- 2. BDDMockito.given()을 사용하여 가독성을 높입니다. ---
      given(userService.authenticate(email, password)).willReturn(mockUser);
      given(request.getSession(true)).willReturn(session);

      // when (무엇을 할 때)
      User resultUser = authService.login(email, password, request);

      // then (결과 확인)
      assertThat(resultUser).isEqualTo(mockUser);

      ArgumentCaptor<UserSessionDto> captor = ArgumentCaptor.forClass(UserSessionDto.class);
      // BDDMockito.then()을 사용하여 검증 부분의 가독성도 높일 수 있습니다.
      then(session).should(times(1)).setAttribute(eq(SessionConst.LOGIN_MEMBER), captor.capture());
      UserSessionDto capturedDto = captor.getValue();

      assertThat(capturedDto.getId()).isEqualTo(mockUser.getId());
      assertThat(capturedDto.getEmail()).isEqualTo(mockUser.getEmail());
    }

    // --- 3. 로그인 실패 시나리오를 더 구체적으로 나눕니다. ---
    @Test
    @DisplayName("실패: 가입되지 않은 이메일로 로그인을 시도한다")
    void login_failure_userNotFound() {
      // given
      String email = "nonexistent@example.com";
      String password = "password123";
      given(userService.authenticate(email, password))
          .willThrow(new IllegalArgumentException("가입되지 않은 이메일입니다."));

      // when & then
      assertThrows(IllegalArgumentException.class,
          () -> authService.login(email, password, request));

      then(request).should(never()).getSession(anyBoolean());
    }

    @Test
    @DisplayName("실패: 비밀번호가 틀려 로그인을 실패한다")
    void login_failure_passwordMismatch() {
      // given
      String email = "test@example.com";
      String password = "wrongPassword";
      given(userService.authenticate(email, password))
          .willThrow(new IllegalArgumentException("비밀번호가 일치하지 않습니다."));

      // when & then
      assertThrows(IllegalArgumentException.class,
          () -> authService.login(email, password, request));

      then(request).should(never()).getSession(anyBoolean());
    }
  }

  @Nested
  @DisplayName("로그아웃 테스트")
  class LogoutTest {

    @Test
    @DisplayName("성공: 세션이 존재할 경우 세션을 무효화한다")
    void logout_with_existing_session() {
      // given
      given(request.getSession(false)).willReturn(session);

      // when
      authService.logout(request);

      // then
      then(session).should(times(1)).invalidate();
    }

    @Test
    @DisplayName("성공: 세션이 없을 경우 아무 작업도 하지 않고 오류도 발생하지 않는다")
    void logout_with_no_session() {
      // given
      given(request.getSession(false)).willReturn(null);

      // when
      authService.logout(request);

      // then
      then(session).should(never()).invalidate();
    }
  }
}