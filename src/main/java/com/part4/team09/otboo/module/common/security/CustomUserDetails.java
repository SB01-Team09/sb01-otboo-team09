package com.part4.team09.otboo.module.common.security;

import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * SecurityContext에 저장할 인증 사용자 정보 클래스
 */
@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

  private UserDto userDto;
  private String password;

  public UUID getId() {
    return userDto.id();
  }

  // 사용자의 권한 정보
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + userDto.role()));
  }

  // 사용자의 비밀번호
  @Override
  public String getPassword() {
    return password;
  }

  // 사용자의 아이디
  @Override
  public String getUsername() {
    return userDto.email();
  }

  /**
   * DaoAuthenticationProvider 에서 아래 메서드들을 사용해,
   * 사용자 계정 상태를 검사하고 false 인 경우 예외를 던져줌.
   */
  
  // 사용자의 계정 잠금 상태를 반환 (false: 잠금 상태)
  @Override
  public boolean isAccountNonLocked() {
    return !userDto.locked();
  }

  // TODO : 임시 비밀번호 구현 시 사용
  // 사용자의 비밀번호 유효 기간 상태를 반환 (false: 기간 만료)
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CustomUserDetails that)) {
      return false;
    }
    return userDto.email().equals(that.userDto.email());
  }

  @Override
  public int hashCode() {
    return Objects.hash(userDto.email());
  }
}
