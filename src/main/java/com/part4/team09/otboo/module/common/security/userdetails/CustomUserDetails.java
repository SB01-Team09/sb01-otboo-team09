package com.part4.team09.otboo.module.common.security.userdetails;

import com.part4.team09.otboo.module.common.security.constants.LoginType;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * SecurityContext에 저장할 인증 사용자 정보 클래스
 */
public class CustomUserDetails implements UserDetails, CustomUserPrincipal {

  private final AuthUserDto userDto;
  private final String password;

  public static CustomUserDetails create(AuthUserDto authUserDto) {
    return new CustomUserDetails(authUserDto, null);
  }

  public static CustomUserDetails createWithPassword(AuthUserDto authUserDto, String password) {
    return new CustomUserDetails(authUserDto, password);
  }

  private CustomUserDetails(AuthUserDto userDto, String password) {
    this.userDto = userDto;
    this.password = password;
  }

  @Override
  public UUID getId() {
    return this.userDto.userId();
  }

  @Override
  public AuthUserDto getAuthUserDto() {
    return this.userDto;
  }

  @Override
  public LoginType getLoginType() {
    return LoginType.BASIC;
  }

  // 사용자의 권한 정보
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + this.userDto.role()));
  }

  // 사용자의 비밀번호
  @Override
  public String getPassword() {
    return this.password;
  }

  // 사용자의 아이디
  @Override
  public String getUsername() {
    return this.userDto.email();
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
