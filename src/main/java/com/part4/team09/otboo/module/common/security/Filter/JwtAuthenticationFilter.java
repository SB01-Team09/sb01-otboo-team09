package com.part4.team09.otboo.module.common.security.Filter;

import com.part4.team09.otboo.module.common.security.handler.CustomAuthenticationEntryPoint;
import com.part4.team09.otboo.module.common.security.jwt.JwtTokenProvider;
import com.part4.team09.otboo.module.common.security.userdetails.CustomUserDetails;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final CustomAuthenticationEntryPoint authenticationEntryPoint;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
    FilterChain filterChain) throws ServletException, IOException {

    String token = extractTokenFromRequest(request);

    if (token != null) {
      try {
        // 유효성 검사
        jwtTokenProvider.validateTokenWithSession(token);

        // 인증 설정
        Authentication authentication = setAuthenticationFromToken(token);

        SecurityContextHolder.getContext().setAuthentication(authentication);

      } catch (AuthenticationException e) {
        authenticationEntryPoint.commence(request, response, e);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  // 인증 설정
  private Authentication setAuthenticationFromToken(String token) {
    // 토큰에서 클레임 가져오기
    AuthUserDto authUserDto = jwtTokenProvider.getAuthUserDtoFromToken(token);

    // CustomUserDetails 생성
    CustomUserDetails principal = CustomUserDetails.create(authUserDto);

    // Authentication 생성
    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
  }

  // 토큰 추출
  private String extractTokenFromRequest(HttpServletRequest request) {
    String authPrefix = "Bearer ";
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorizationHeader != null) {
      return authorizationHeader.substring(authPrefix.length());
    }
    return null;
  }

  // 화이트 리스트
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    String method = request.getMethod();
    return !path.startsWith("/api/")
      || ("/api/users".equals(path) && "POST".equalsIgnoreCase(method))
      || path.startsWith("/api/auth/")
      || path.startsWith("/swagger-ui")
      || path.startsWith("/v3/api-docs");
  }
}
