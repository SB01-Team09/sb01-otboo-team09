package com.part4.team09.otboo.module.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.common.dto.ErrorResponse;
import com.part4.team09.otboo.module.common.util.IpUtils;
import com.part4.team09.otboo.module.domain.auth.exception.AuthErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * 로그인 시도 실패 시 핸들러 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonLoginFailureHandler implements AuthenticationFailureHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
    AuthenticationException exception) throws IOException, ServletException {

    AuthErrorCode errorCode;

    // 로그인 실패 예외 확인
    if (exception instanceof BadCredentialsException) {
      errorCode = AuthErrorCode.INVALID_CREDENTIALS;
    } else if (exception instanceof CredentialsExpiredException) {
      errorCode = AuthErrorCode.CREDENTIALS_EXPIRED;
    } else if (exception instanceof LockedException) {
      errorCode = AuthErrorCode.ACCOUNT_LOCKED;
    } else {
      errorCode = AuthErrorCode.AUTHENTICATION_FAILED;
    }

    log.info("로그인 실패 (이유: {}, IP: {})", exception.getClass().getSimpleName(),
      IpUtils.getClientIp(request));

    ErrorResponse errorResponse = ErrorResponse.of(
      AuthenticationException.class.getSimpleName(),
      errorCode.getMessage()
    );

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    objectMapper.writeValue(response.getWriter(), errorResponse);
  }
}
