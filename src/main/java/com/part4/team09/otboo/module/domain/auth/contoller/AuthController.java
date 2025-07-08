package com.part4.team09.otboo.module.domain.auth.contoller;

import com.part4.team09.otboo.module.common.security.jwt.GeneratedToken;
import com.part4.team09.otboo.module.domain.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  // csrf 토큰 조회
  @GetMapping("/csrf-token")
  public ResponseEntity<CsrfToken> getCsrfToken(CsrfToken csrfToken) {
    return ResponseEntity.ok(csrfToken);
  }

  // access 토큰 조회
  @GetMapping("/me")
  public ResponseEntity<String> getAccessToken(@CookieValue("refresh_token") String refreshToken) {
    String accessToken = authService.getAccessTokenByRefreshToken(refreshToken);
    return ResponseEntity.ok(accessToken);
  }

  // refresh 토큰 재발급
  @PostMapping("/refresh")
  public ResponseEntity<String> refreshTokens(
    @CookieValue("refresh_token") String refreshToken,
    HttpServletResponse response
  ) {
    GeneratedToken generatedToken = authService.refreshTokens(refreshToken);
    Cookie refreshCookie = new Cookie("refresh_token", generatedToken.refreshToken());
    response.addCookie(refreshCookie);
    return ResponseEntity.ok(generatedToken.accessToken());
  }

  // 비밀번호 초기화

}
