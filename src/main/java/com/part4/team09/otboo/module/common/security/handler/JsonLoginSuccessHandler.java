package com.part4.team09.otboo.module.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.common.security.CustomUserDetails;
import com.part4.team09.otboo.module.common.security.jwt.GeneratedToken;
import com.part4.team09.otboo.module.common.security.jwt.JwtTokenProvider;
import com.part4.team09.otboo.module.common.util.CookieUtil;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.auth.dto.TempPasswordMetadata;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 로그인 성공 시 핸들러 클래스
 */
@Slf4j
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
    TempPasswordMetadata tempPasswordMeta = checkTempPasswordMetadata(authentication);
    GeneratedToken generatedToken = jwtTokenProvider.generateToken(authUserDto, tempPasswordMeta);

    // 쿠키 생성 (refresh token)
    Cookie refreshTokenCookie = CookieUtil.createRefreshTokenCookie(generatedToken.refreshToken());
    response.addCookie(refreshTokenCookie);

    // 응답 설정
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setStatus(HttpServletResponse.SC_OK);

    // 액세스 토큰 응답
    objectMapper.writeValue(response.getWriter(), generatedToken.accessToken());
  }

  // Authentication 객체에서 임시 비빌번호 인증인지 확인
  private TempPasswordMetadata checkTempPasswordMetadata(Authentication authentication) {
    Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
    return (TempPasswordMetadata) details.get("tempPassword");
  }
}
