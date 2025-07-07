package com.part4.team09.otboo.module.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.common.dto.ErrorResponse;
import com.part4.team09.otboo.module.common.util.IpUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 인증 실패 시 동작 (401)
 * jwt가 없거나, 잘못된 토큰, 인증 안된 사용자 등등
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
    AuthenticationException authException) throws IOException, ServletException {

    log.info("인증 실패 (이유: {} - {}, IP: {})",
      authException.getClass().getSimpleName(),
      authException.getMessage(),
      IpUtils.getClientIp(request));

    ErrorResponse errorResponse = ErrorResponse.of(
      AuthenticationException.class.getSimpleName(),
      authException.getMessage()
    );

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    objectMapper.writeValue(response.getWriter(), errorResponse);
  }
}
