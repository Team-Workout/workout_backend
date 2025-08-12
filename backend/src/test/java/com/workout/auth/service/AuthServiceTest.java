package com.workout.auth.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.user.domain.Role;
import com.workout.user.domain.User;
import com.workout.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.SecurityContextRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

  @Mock
  private UserService userService;

  @Mock
  private SecurityContextRepository securityContextRepository;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @InjectMocks
  private AuthService authService;

  private User mockUser;

  @BeforeEach
  void setUp() {
    mockUser = User.builder()
        .id(1L)
        .email("test@example.com")
        .name("테스트유저")
        .password("encodedPassword")
        .role(Role.USER) // 테스트를 위한 역할 설정
        .build();
  }


  @Nested
  @DisplayName("로그인 테스트")
  class LoginTest {

    @Test
    @DisplayName("성공: 올바른 자격 증명으로 로그인 시, 정확한 UserPrincipal을 담은 SecurityContext가 저장된다")
    void login_success() {
      // given
      given(userService.authenticate(mockUser.getEmail(), "password123")).willReturn(mockUser);

      // when
      User resultUser = authService.login(mockUser.getEmail(), "password123", request, response);

      // then: 반환된 유저가 Mock 유저와 동일한지 확인
      assertThat(resultUser).isEqualTo(mockUser);

      // then: SecurityContextRepository.saveContext가 호출되었는지 검증
      ArgumentCaptor<SecurityContext> contextCaptor = ArgumentCaptor.forClass(SecurityContext.class);
      then(securityContextRepository).should(times(1)).saveContext(contextCaptor.capture(), eq(request), eq(response));

      // and: 캡처된 SecurityContext의 Principal을 상세히 검증
      SecurityContext capturedContext = contextCaptor.getValue();
      Object principal = capturedContext.getAuthentication().getPrincipal();

      assertThat(principal).isInstanceOf(UserPrincipal.class);
      UserPrincipal userPrincipal = (UserPrincipal) principal;

      assertThat(userPrincipal.getUserId()).isEqualTo(mockUser.getId()); //
      assertThat(userPrincipal.getUsername()).isEqualTo(mockUser.getEmail()); //
      assertThat(userPrincipal.getPassword()).isEqualTo(mockUser.getPassword()); //
      assertThat(userPrincipal.getAuthorities()).hasSize(1);
      assertThat(userPrincipal.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER"); //
    }

    @Test
    @DisplayName("실패: 가입되지 않은 이메일로 로그인을 시도하면 예외가 발생한다")
    void login_failure_userNotFound() {
      // given
      String email = "nonexistent@example.com";
      String password = "password123";
      given(userService.authenticate(email, password))
          .willThrow(new IllegalArgumentException("가입되지 않은 이메일입니다."));

      // when & then
      assertThrows(IllegalArgumentException.class,
          () -> authService.login(email, password, request, response));

      // and: SecurityContextRepository는 호출되지 않아야 한다
      then(securityContextRepository).should(never()).saveContext(any(), any(), any());
    }
  }
}