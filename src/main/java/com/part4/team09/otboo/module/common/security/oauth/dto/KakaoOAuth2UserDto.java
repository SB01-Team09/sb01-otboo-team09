package com.part4.team09.otboo.module.common.security.oauth.dto;

import java.util.Map;

public class KakaoOAuth2UserDto implements OAuth2UserDto {

  private String id;
  private String name;
  private String email;
  private String profileImageUrl;

  public KakaoOAuth2UserDto(Map<String, Object> attributes) {
    Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
    Map<String, Object> profile = (Map<String, Object>) account.get("profile");

    this.id = attributes.get("id").toString();
    this.name = profile.get("nickname").toString();
    this.email = this.name + "@kakao.com";
    this.profileImageUrl = profile.get("thumbnail_image_url") == null
      ? null
      : profile.get("thumbnail_image_url").toString();
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getEmail() {
    return this.email;
  }

  @Override
  public String getProfileImageUrl() {
    return this.profileImageUrl;
  }
}
