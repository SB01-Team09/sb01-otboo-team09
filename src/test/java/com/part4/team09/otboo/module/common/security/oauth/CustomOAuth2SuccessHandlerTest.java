package com.part4.team09.otboo.module.common.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.common.security.constants.AuthCookieNames;
import com.part4.team09.otboo.module.common.security.jwt.GeneratedToken;
import com.part4.team09.otboo.module.common.security.jwt.JwtTokenProvider;
import com.part4.team09.otboo.module.common.security.userdetails.CustomOAuth2User;
import com.part4.team09.otboo.module.common.util.CookieUtil;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.auth.dto.TempPasswordMetadata;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2SuccessHandlerTest {

  @Spy
  private CookieUtil cookieUtil;

  @Mock
  private CustomOAuth2User customOAuth2User;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @InjectMocks
  private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

  @Test
  @DisplayName("소셜 로그인 성공 시 토큰 생성 및 응답")
  void social_login_success() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    Authentication authentication = mock(Authentication.class);

    // 인증 정보
    when(authentication.getPrincipal()).thenReturn(customOAuth2User);

    AuthUserDto authUserDto = mock(AuthUserDto.class);
    when(customOAuth2User.getAuthUserDto()).thenReturn(authUserDto);

    // 토큰 생성
    GeneratedToken generatedToken = new GeneratedToken("accessToken", "refreshToken");
    when(jwtTokenProvider.generateToken(eq(authUserDto), any(TempPasswordMetadata.class)))
      .thenReturn(generatedToken);

    // when
    customOAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

    // then
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
    assertThat(response.getRedirectedUrl()).isEqualTo("/#/recommendation");
    assertThat(response.getCookies())
      .extracting(Cookie::getName)
      .contains(AuthCookieNames.REFRESH_TOKEN_COOKIE_NAME);
  }
}