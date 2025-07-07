package com.part4.team09.otboo.module.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.common.dto.ErrorResponse;
import com.part4.team09.otboo.module.common.util.IpUtils;
import com.part4.team09.otboo.module.domain.auth.exception.AuthErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 인가 실패 시 동작 (401)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
    AccessDeniedException accessDeniedException) throws IOException, ServletException {

    log.info("접근 권한 없음 (이유: {} - {}, IP: {})",
      accessDeniedException.getClass().getSimpleName(),
      accessDeniedException.getMessage(),
      IpUtils.getClientIp(request));

    AuthErrorCode errorCode = AuthErrorCode.ACCESS_DENIED;

    ErrorResponse errorResponse = ErrorResponse.of(
      AuthenticationException.class.getSimpleName(),
      errorCode.getMessage()
    );

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    objectMapper.writeValue(response.getWriter(), errorResponse);
  }
}
