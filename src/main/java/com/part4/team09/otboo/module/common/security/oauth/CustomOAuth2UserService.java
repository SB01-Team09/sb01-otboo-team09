package com.part4.team09.otboo.module.common.security.oauth;

import com.part4.team09.otboo.module.common.security.oauth.OAuthProvider.SocialType;
import com.part4.team09.otboo.module.common.security.oauth.dto.OAuth2UserDto;
import com.part4.team09.otboo.module.common.security.oauth.dto.OAuthAttributes;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.auth.mapper.AuthUserMapper;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소셜 로그인 인증 후 유저 확인
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final OAuthProviderRepository oAuthProviderRepository;
  private final UserRepository userRepository;
  private final AuthUserMapper authUserMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    log.info("outh 인증시작");

    // 로그인이 성공하면 spring 이 알아서 userRequest 데이터를 보내준다. (accessToken, provider 정보)
    // 액세스 토큰으로 사용자 정보 조회
    OAuth2User oAuth2User = super.loadUser(userRequest);

    // provider(google, kakao) 확인, 소셜 타입 확인
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    SocialType socialType = getSocialType(registrationId);

    // OAuth2 provider 별 사용자 식별 속성명
    String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
      .getUserInfoEndpoint().getUserNameAttributeName();

    // 사용자 정보
    Map<String, Object> attributes = oAuth2User.getAttributes();

    // provider 별로 사용자 정보 파싱
    OAuthAttributes oAuthAttributes = OAuthAttributes.of(socialType, userNameAttributeName,
      attributes);

    // provider, providerId로 유저 정보 가져오기
    User user = getOrCreateUser(socialType, oAuthAttributes);
    AuthUserDto authUserDto = authUserMapper.toAuthUserDto(user);

    log.info("outh 유저 정보 조회까지 성공");

    // principal
    return CustomOAuth2User.create(authUserDto, attributes, userNameAttributeName);
  }

  private SocialType getSocialType(String registrationId) {
    try {
      return SocialType.valueOf(registrationId.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("지원하지 않는 소셜 타입: {}", registrationId);
      throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인 타입");
    }
  }

  private User getOrCreateUser(SocialType socialType, OAuthAttributes oAuthAttributes) {
    OAuth2UserDto oAuth2UserDto = oAuthAttributes.getOAuth2UserDto();
    String providerId = oAuth2UserDto.getId();

    Optional<OAuthProvider> oAuthProvider = oAuthProviderRepository.findByProviderAndProviderId(
      socialType, providerId);

    // 소셜 로그인 정보가 없는 경우
    if (oAuthProvider.isEmpty()) {

      log.info("기존 소셜 로그인 정보 없음");

      // 일반 로그인 유저 확인
      userRepository.findByEmail(oAuth2UserDto.getEmail()).ifPresent(user -> {
        log.info("이미 가입된 이메일: {}", oAuth2UserDto.getEmail());
        throw new OAuth2AuthenticationException("이미 가입된 계정");
      });

      // 가입 정보가 없으면 새로운 유저 생성
      return createNewUserFromSocial(socialType, oAuthAttributes);
    }

    log.info("기존 소셜 로그인 정보 있음");
    return findUserByEmailOrThrow(oAuth2UserDto.getEmail());
  }

  // 새로운 유저 생성
  private User createNewUserFromSocial(SocialType socialType, OAuthAttributes oAuthAttributes) {

    log.info("새로운 유저 생성 진입");

    OAuth2UserDto oAuth2UserDto = oAuthAttributes.getOAuth2UserDto();
    String dummyPassword = passwordEncoder.encode(UUID.randomUUID().toString());

    User user = User.createUser(oAuth2UserDto.getEmail(), oAuth2UserDto.getName(), dummyPassword);
    user.updateProfileImageUrl(oAuth2UserDto.getProfileImageUrl());
    User savedUser = userRepository.save(user);

    OAuthProvider oAuthProvider = OAuthProvider.create(savedUser.getId(), socialType,
      oAuth2UserDto.getId());

    userRepository.save(user);
    oAuthProviderRepository.save(oAuthProvider);

    return user;
  }

  private User findUserByEmailOrThrow(String email) {
    return userRepository.findByEmail(email)
      .orElseThrow(() -> UserNotFoundException.withEmail(email));
  }
}