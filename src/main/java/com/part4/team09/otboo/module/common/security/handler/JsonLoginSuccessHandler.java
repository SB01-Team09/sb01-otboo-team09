package com.part4.team09.otboo.module.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.common.security.AuthCookieNames;
import com.part4.team09.otboo.module.common.security.CustomUserDetails;
import com.part4.team09.otboo.module.common.security.jwt.GeneratedToken;
import com.part4.team09.otboo.module.common.security.jwt.JwtTokenProvider;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 로그인 성공 시 핸들러 클래스
 */
@Component
@RequiredArgsConstructor
public class JsonLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;
  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
    Authentication authentication) throws IOException, ServletException {

    // 인증 정보
    CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
    AuthUserDto authUserDto = principal.getUserDto();

    // 토큰 발급
    GeneratedToken generatedToken = jwtTokenProvider.generateToken(authUserDto);

    // 쿠키 생성 (refresh token)
    Cookie tempCookie = new Cookie(AuthCookieNames.REFRESH_TOKEN_COOKIE_NAME,
      generatedToken.refreshToken());
    tempCookie.setHttpOnly(true);

    // 응답 설정
    response.addCookie(tempCookie);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setStatus(HttpServletResponse.SC_OK);

    // 액세스 토큰 응답
    objectMapper.writeValue(response.getWriter(), generatedToken.accessToken());
  }
}
