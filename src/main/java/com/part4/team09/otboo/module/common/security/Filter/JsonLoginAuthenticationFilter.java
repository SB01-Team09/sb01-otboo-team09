package com.part4.team09.otboo.module.common.security.Filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.domain.auth.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Json login 인증 필터
 */
@Slf4j
public class JsonLoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final ObjectMapper objectMapper;

  public JsonLoginAuthenticationFilter(AuthenticationManager authenticationManager,
    ObjectMapper objectMapper) {
    super(authenticationManager);
    this.objectMapper = objectMapper;
    setFilterProcessesUrl("/api/auth/sign-in");
  }

  // 인증 시도
  public Authentication attemptAuthentication(
    HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

    // method 확인
    if (!request.getMethod().equals("POST")) {
      throw new AuthenticationServiceException("지원하지 않는 메소드입니다. (" + request.getMethod() + ")");
    }

    // json parsing
    try {
      LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(),
        LoginRequest.class);

      UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        loginRequest.email(), loginRequest.password());

      // 인증 매니저에게 인증 요청
      return this.getAuthenticationManager().authenticate(authToken);

    } catch (IOException e) {
      throw new AuthenticationServiceException("로그인 요청 파싱에 실패하였습니다.");
    }
  }
}
