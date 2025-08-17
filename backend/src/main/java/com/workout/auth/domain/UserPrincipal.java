package com.workout.auth.domain;

import com.workout.user.domain.Member;
import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class UserPrincipal implements UserDetails {

  private final Long userId;
  private final String email;
  private final String password;
  private final Collection<? extends GrantedAuthority> authorities;

  public UserPrincipal(Member member) {
    this.userId = member.getId();
    this.email = member.getEmail();
    this.password = member.getPassword();
    this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
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

  // 계정 만료, 잠금, 비번 만료, 활성화 여부 등은 필요에 따라 구현
  @Override
  public boolean isAccountNonExpired() { return true; }
  @Override
  public boolean isAccountNonLocked() { return true; }
  @Override
  public boolean isCredentialsNonExpired() { return true; }
  @Override
  public boolean isEnabled() { return true; }
}