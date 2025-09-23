package com.workout.auth.domain;

import com.workout.member.domain.Member;
import java.util.Collection;
import java.util.Collections;
import java.util.Map; // ⬅️ 추가
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User; // ⬅️ 추가

@Getter
public class UserPrincipal implements UserDetails, OAuth2User { // ⬅️ OAuth2User 구현 추가

  private final Long userId;
  private final String email;
  private final String password;
  private final Collection<? extends GrantedAuthority> authorities;
  private Map<String, Object> attributes;

  public UserPrincipal(Member member) {
    this.userId = member.getId();
    this.email = member.getEmail();
    this.password = member.getPassword();
    this.authorities = Collections.singletonList(
        new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
  }

  public UserPrincipal(Member member, Map<String, Object> attributes) {
    this(member); // 기존 생성자 호출로 기본 정보 초기화
    this.attributes = attributes;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getName() {
    // OAuth2 공급자로부터 받은 고유 식별값을 반환합니다.
    // 여기서는 이메일을 사용하지만, 구글의 'sub' 필드를 사용하는 것이 더 일반적입니다.
    return this.email;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() { return true; }

  @Override
  public boolean isAccountNonLocked() { return true; }

  @Override
  public boolean isCredentialsNonExpired() { return true; }

  @Override
  public boolean isEnabled() { return true; }
}