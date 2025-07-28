package com.part4.team09.otboo.module.common.security.oauth.dto;

import com.part4.team09.otboo.module.common.security.oauth.entity.OAuthProvider.SocialType;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OAuthAttributes {

  private String nameAttributeKey;
  private OAuth2UserDto oAuth2UserDto;

  public static OAuthAttributes of(SocialType socialType, String nameAttributeKey,
    Map<String, Object> attributes) {

    if (socialType == SocialType.KAKAO) {
      return ofKakao(nameAttributeKey, attributes);
    } else if (socialType == SocialType.GOOGLE) {
      return ofGoogle(nameAttributeKey, attributes);
    }
    return null;
  }

  private static OAuthAttributes ofKakao(String userNameAttributeName,
    Map<String, Object> attributes) {
    return OAuthAttributes.builder()
      .nameAttributeKey(userNameAttributeName)
      .oAuth2UserDto(new KakaoOAuth2UserDto(attributes))
      .build();
  }

  private static OAuthAttributes ofGoogle(String userNameAttributeName,
    Map<String, Object> attributes) {
    return OAuthAttributes.builder()
      .nameAttributeKey(userNameAttributeName)
      .oAuth2UserDto(new GoogleOAuth2UserDto(attributes))
      .build();
  }
}
