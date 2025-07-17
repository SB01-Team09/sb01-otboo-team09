package com.part4.team09.otboo.module.common.security.handler;

import com.part4.team09.otboo.module.common.security.AuthCookieNames;
import com.part4.team09.otboo.module.common.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
    Authentication authentication) {

    log.info("왜 로그아웃 진입?");

    extractRefreshTokenFromRequest(request)
      .ifPresent(refreshToken -> {
        String userEmail = jwtTokenProvider.getSubjectFromToken(refreshToken);
        jwtTokenProvider.invalidateRefreshToken(refreshToken);
        invalidateRefreshTokenCookie(response);
        log.info("로그아웃되었습니다. (userEmail: {})", userEmail);
      });
  }

  // refresh token 추출
  private Optional<String> extractRefreshTokenFromRequest(HttpServletRequest request) {
    return Arrays.stream(request.getCookies())
      .filter(cookie -> cookie.getName().equals(AuthCookieNames.REFRESH_TOKEN_COOKIE_NAME))
      .findFirst()
      .map(Cookie::getValue);
  }

  // refresh token 쿠키 무효화
  private void invalidateRefreshTokenCookie(HttpServletResponse response) {
    Cookie refreshTokenCookie = new Cookie(AuthCookieNames.REFRESH_TOKEN_COOKIE_NAME, "");
    refreshTokenCookie.setMaxAge(0);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setPath("/");
    response.addCookie(refreshTokenCookie);
  }
}
