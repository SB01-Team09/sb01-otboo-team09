package com.part4.team09.otboo.module.common.security.oauth;

import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final OAuthProviderRepository oAuthProviderRepository;
  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    // 로그인이 성공하면 spring 이 알아서 userRequest 데이터를 보내준다. (accessToken, provider 정보)
    // 액세스 토큰으로 사용자 정보 조회
    OAuth2User oAuth2User = super.loadUser(userRequest);

    // provider(google, kakao) 확인
    String registrationId = userRequest.getClientRegistration().getRegistrationId();

    // OAuth2 provider 별 사용자 식별 속성명
    String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
      .getUserInfoEndpoint().getUserNameAttributeName();

    // 사용자 정보
    Map<String, Object> attributes = oAuth2User.getAttributes();

    // provider 별로 사용자 정보 파싱

    // provider, providerId로 해당 인증정보가 있는지 조회
//    String provider = oAuthProviderRepository.findByProviderAndProviderId()

    // 있다면 해당 유저 정보 조회 후 반환
    // 없다면 email 로 유저 정보 조회 (카카오는 불가??)
    // 있다면 해당 소셜 연동 데이터 생성 후 유저 정보 반환

    // principal 반환
    return CustomOAuth2User.create(null, attributes, userNameAttributeName);
  }
}