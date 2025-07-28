package com.part4.team09.otboo.module.common.security.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.common.security.constants.AuthCookieNames;
import com.part4.team09.otboo.module.common.security.jwt.JwtTokenProvider;
import com.part4.team09.otboo.module.domain.notification.sse.SseService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class CustomLogoutHandlerTest {

  @Mock
  JwtTokenProvider jwtTokenProvider;

  @Mock
  SseService sseService;

  @InjectMocks
  CustomLogoutHandler logoutHandler;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Mock
  Authentication authentication;

  @DisplayName("RefreshToken이 있을 때 정상 로그아웃")
  @Test
  void logout_withValidRefreshToken_shouldInvalidateAndDisconnect() {
    // given
    String refreshToken = "valid-refresh-token";
    UUID userId = UUID.randomUUID();

    Cookie cookie = new Cookie(AuthCookieNames.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
    when(request.getCookies()).thenReturn(new Cookie[]{cookie});
    when(jwtTokenProvider.getUserIdFromToken(refreshToken))
      .thenReturn(userId);

    // when
    logoutHandler.logout(request, response, authentication);

    // then
    verify(jwtTokenProvider).invalidateRefreshToken(refreshToken);
    verify(sseService).disconnectAllEmitters(eq(userId), any(String.class));
    verify(response).addCookie(any(Cookie.class));
  }
}