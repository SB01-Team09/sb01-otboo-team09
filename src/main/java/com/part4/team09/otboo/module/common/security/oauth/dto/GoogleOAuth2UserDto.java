package com.part4.team09.otboo.module.common.security.oauth.dto;

import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GoogleOAuth2UserDto implements OAuth2UserDto {

  private final Map<String, Object> attributes;

  @Override
  public String getId() {
    return attributes.get("sub").toString();
  }

  @Override
  public String getName() {
    return attributes.get("name").toString();
  }

  @Override
  public String getEmail() {
    return attributes.get("email").toString();
  }

  @Override
  public String getProfileImageUrl() {
    return attributes.get("picture") == null
      ? null
      : attributes.get("picture").toString();
  }
}
