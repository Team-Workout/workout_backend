package com.workout.auth.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.user.domain.Role;
import com.workout.user.domain.User;
import com.workout.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;

import org.springframework.security.web.context.SecurityContextRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

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

  @Nested
  @DisplayName("로그인 테스트")
  class LoginTest {

    @Test
    @DisplayName("성공: 단일 권한 사용자로 로그인 시, 정확한 UserPrincipal을 담은 SecurityContext가 저장된다")
    void login_success_withSingleRole() {
      // given: 단일 역할을 가진 사용자 설정
      User userWithSingleRole = User.builder()
          .id(1L)
          .email("user@example.com")
          .password("encodedPassword")
          .role(Role.USER)
          .build();
      given(userService.authenticate("user@example.com", "password123")).willReturn(userWithSingleRole);

      // when
      User resultUser = authService.login("user@example.com", "password123", request, response);

      // then: 반환된 유저가 Mock 유저와 동일한지 확인
      assertThat(resultUser).isEqualTo(userWithSingleRole);

      // and: SecurityContextRepository.saveContext가 정확히 1번 호출되었는지 검증
      // ArgumentCaptor: 메소드 내부에서 생성되어 외부로 노출되지 않는 객체를 캡처하여 검증하기 위한 강력한 도구
      ArgumentCaptor<SecurityContext> contextCaptor = ArgumentCaptor.forClass(SecurityContext.class);
      then(securityContextRepository).should().saveContext(contextCaptor.capture(), eq(request), eq(response));

      // and: 캡처된 SecurityContext의 Principal을 상세히 검증
      SecurityContext capturedContext = contextCaptor.getValue();
      Object principal = capturedContext.getAuthentication().getPrincipal();

      assertThat(principal).isInstanceOf(UserPrincipal.class);
      UserPrincipal userPrincipal = (UserPrincipal) principal;

      assertThat(userPrincipal.getUserId()).isEqualTo(userWithSingleRole.getId());
      assertThat(userPrincipal.getUsername()).isEqualTo(userWithSingleRole.getEmail());
      assertThat(userPrincipal.getPassword()).isEqualTo(userWithSingleRole.getPassword());
      assertThat(userPrincipal.getAuthorities())
          .hasSize(1)
          .extracting(GrantedAuthority::getAuthority)
          .containsExactly("ROLE_USER");
    }

    // [보완] 테스트 커버리지 확장을 위한 다중 권한 사용자 케이스 추가
    @Test
    @DisplayName("성공: 다중 권한 사용자로 로그인 시, 모든 권한이 정확히 포함된 SecurityContext가 저장된다")
    void login_success_withMultipleRoles() {
      // given: 다중 역할을 가진 사용자 설정 (USER, ADMIN)
      User userWithMultipleRoles = User.builder()
          .id(2L)
          .email("admin@example.com")
          .password("encodedAdminPassword")
          .role(Role.ADMIN) // User 엔티티가 여러 역할을 가질 수 있다고 가정 (예: Set<Role> roles)
          .build();
      // 만약 User 엔티티가 Set<Role>을 지원한다면 아래와 같이 given을 설정할 수 있습니다.
      // 현재는 단일 Role만 있으므로, Role.ADMIN으로 테스트 진행.
      // 만약 User.getRole()이 Set<Role>을 반환한다면, UserPrincipal의 로직도 그에 맞게 수정되어야 합니다.
      given(userService.authenticate("admin@example.com", "adminPass")).willReturn(userWithMultipleRoles);

      // when
      User resultUser = authService.login("admin@example.com", "adminPass", request, response);

      // then
      assertThat(resultUser).isEqualTo(userWithMultipleRoles);

      ArgumentCaptor<SecurityContext> contextCaptor = ArgumentCaptor.forClass(SecurityContext.class);
      then(securityContextRepository).should().saveContext(contextCaptor.capture(), eq(request), eq(response));

      UserPrincipal userPrincipal = (UserPrincipal) contextCaptor.getValue().getAuthentication().getPrincipal();
      assertThat(userPrincipal.getAuthorities())
          .extracting(GrantedAuthority::getAuthority)
          // User 엔티티의 Role이 Set<Role>을 지원하게 되면 "ROLE_USER", "ROLE_ADMIN" 등으로 검증
          .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("실패: 가입되지 않은 이메일로 로그인을 시도하면 예외가 발생하고 세션이 저장되지 않는다")
    void login_failure_userNotFound() {
      // given: 서비스가 예외를 던지도록 설정
      String email = "nonexistent@example.com";
      String password = "password123";
      given(userService.authenticate(email, password))
          .willThrow(new IllegalArgumentException("가입되지 않은 이메일입니다."));

      // when & then: 지정된 예외가 발생하는지 검증
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> authService.login(email, password, request, response));
      assertThat(exception.getMessage()).isEqualTo("가입되지 않은 이메일입니다.");

      // and: 예외 발생 시, SecurityContextRepository.saveContext가 '절대' 호출되지 않았는지 검증
      // 이는 실패 경로에서 의도치 않은 부수 효과(side effect)가 없음을 보장하는 중요한 검증
      then(securityContextRepository).should(never()).saveContext(any(), any(), any());
    }
  }
}