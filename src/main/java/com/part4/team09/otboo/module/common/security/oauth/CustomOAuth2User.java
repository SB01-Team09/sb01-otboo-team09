package com.part4.team09.otboo.module.common.security.oauth;

import com.part4.team09.otboo.module.common.security.CustomUserPrincipal;
import com.part4.team09.otboo.module.common.security.constants.LoginType;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * OAuth 로그인 후 인증 객체
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomOAuth2User implements OAuth2User, CustomUserPrincipal {

  private final AuthUserDto userDto;
  private final Map<String, Object> attributes;
  private final String userNameAttributeName;

  public static CustomOAuth2User create(AuthUserDto authUserDto,
    Map<String, Object> attributes, String userNameAttributeName) {
    return new CustomOAuth2User(authUserDto, attributes, userNameAttributeName);
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
    return LoginType.OAUTH;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return this.attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + this.userDto.role()));
  }

  @Override
  public String getName() {
    return this.attributes.get(this.userNameAttributeName).toString();
  }
}
