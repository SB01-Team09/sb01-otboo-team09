package com.part4.team09.otboo.module.common.security.oauth;

import com.part4.team09.otboo.module.common.security.jwt.GeneratedToken;
import com.part4.team09.otboo.module.common.security.jwt.JwtTokenProvider;
import com.part4.team09.otboo.module.common.security.userdetails.CustomOAuth2User;
import com.part4.team09.otboo.module.common.util.CookieUtils;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.auth.dto.TempPasswordMetadata;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * OAuth 로그인 성공 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
    Authentication authentication) throws IOException, ServletException {

    // 인증 정보
    CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
    AuthUserDto authUserDto = principal.getAuthUserDto();

    // 토큰 생성
    TempPasswordMetadata tempPasswordMeta = TempPasswordMetadata.notUsed();
    GeneratedToken generatedToken = jwtTokenProvider.generateToken(authUserDto, tempPasswordMeta);

    // 쿠키 설정
    Cookie refreshTokenCookie = CookieUtils.createRefreshTokenCookie(generatedToken.refreshToken());
    response.addCookie(refreshTokenCookie);

    // 응답 설정
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setStatus(HttpServletResponse.SC_OK);

    log.error("Oauth 로그인 성공 및 응답");

    getRedirectStrategy().sendRedirect(request, response, "/#/recommendation");

  }
}