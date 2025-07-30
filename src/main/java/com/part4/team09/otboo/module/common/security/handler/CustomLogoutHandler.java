package com.part4.team09.otboo.module.common.security.handler;

import com.part4.team09.otboo.module.common.security.constants.AuthCookieNames;
import com.part4.team09.otboo.module.common.security.jwt.JwtTokenProvider;
import com.part4.team09.otboo.module.common.util.CookieUtils;
import com.part4.team09.otboo.module.domain.notification.sse.SseService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
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
  private final SseService sseService;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
    Authentication authentication) {

    extractRefreshTokenFromRequest(request)
      .ifPresent(refreshToken -> {
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        jwtTokenProvider.invalidateRefreshToken(refreshToken);
        invalidateRefreshTokenCookie(response);
        sseService.disconnectAllEmitters(userId, "로그아웃으로 연결 해제");
        log.info("로그아웃되었습니다. (userId: {})", userId);
      });
  }

  // refresh token 추출
  private Optional<String> extractRefreshTokenFromRequest(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    return Arrays.stream(request.getCookies())
      .filter(cookie -> cookie.getName().equals(AuthCookieNames.REFRESH_TOKEN_COOKIE_NAME))
      .findFirst()
      .map(Cookie::getValue);
  }

  // refresh token 쿠키 무효화
  private void invalidateRefreshTokenCookie(HttpServletResponse response) {
    Cookie refreshTokenCookie = CookieUtils.expireRefreshTokenCookie();
    response.addCookie(refreshTokenCookie);
  }
}
