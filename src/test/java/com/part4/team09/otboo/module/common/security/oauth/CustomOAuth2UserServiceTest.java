package com.part4.team09.otboo.module.common.security.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.common.security.oauth.dto.GoogleOAuth2UserDto;
import com.part4.team09.otboo.module.common.security.oauth.dto.OAuth2UserDto;
import com.part4.team09.otboo.module.common.security.oauth.dto.OAuthAttributes;
import com.part4.team09.otboo.module.common.security.oauth.entity.OAuthProvider;
import com.part4.team09.otboo.module.common.security.oauth.entity.OAuthProvider.SocialType;
import com.part4.team09.otboo.module.domain.auth.mapper.AuthUserMapper;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

  @Mock
  private OAuthProviderRepository oAuthProviderRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private AuthUserMapper authUserMapper;
  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private CustomOAuth2UserService customOAuth2UserService;

  private OAuth2UserDto dto;
  private OAuthAttributes attr;

  @BeforeEach
  void setUp() {
    dto = new GoogleOAuth2UserDto(Map.of(
      "sub", "123",
      "email", "test@test.com",
      "name", "test",
      "picture", "http://test.com/test.png"
    ));
    attr = new OAuthAttributes("sub", dto);
  }

  @Test
  @DisplayName("소셜 정보가 존재하면 이메일로 유저를 조회해서 반환한다")
  void getOrCreateUser_shouldReturnUser_whenProviderExists() {
    // given
    User mockUser = mock(User.class);
    when(oAuthProviderRepository.findByProviderAndProviderId(SocialType.GOOGLE, "123"))
      .thenReturn(Optional.of(mock()));
    when(userRepository.findByEmail("test@test.com"))
      .thenReturn(Optional.of(mockUser));

    // when
    User result = customOAuth2UserService.getOrCreateUser(SocialType.GOOGLE, attr);

    // then
    assertEquals(mockUser, result);
  }

  @Test
  @DisplayName("소셜 정보가 없고 동일 이메일이 존재하면 예외가 발생한다")
  void getOrCreateUser_shouldThrow_whenEmailAlreadyExists() {
    // given
    when(oAuthProviderRepository.findByProviderAndProviderId(SocialType.GOOGLE, "123"))
      .thenReturn(Optional.empty());
    when(userRepository.findByEmail("test@test.com"))
      .thenReturn(Optional.of(mock()));

    // when & then
    assertThrows(OAuth2AuthenticationException.class, () ->
      customOAuth2UserService.getOrCreateUser(SocialType.GOOGLE, attr));
  }

  @Test
  @DisplayName("소셜 정보와 이메일이 모두 없으면 새로운 유저를 생성한다")
  void getOrCreateUser_shouldCreateUser_whenNoSocialOrEmailExists() {
    // given
    when(oAuthProviderRepository.findByProviderAndProviderId(SocialType.GOOGLE, "123"))
      .thenReturn(Optional.empty());
    when(userRepository.findByEmail("test@test.com"))
      .thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString()))
      .thenReturn("dummy-password");

    User user = User.createUser("test@test.com", "test", "dummy-password");
    when(userRepository.save(any(User.class))).thenReturn(user);

    // when
    User result = customOAuth2UserService.getOrCreateUser(SocialType.GOOGLE, attr);

    // then
    assertEquals(user, result);
    verify(userRepository).save(any(User.class)); // 유저, OAuthProvider 등록
    verify(oAuthProviderRepository).save(any(OAuthProvider.class));
  }
}