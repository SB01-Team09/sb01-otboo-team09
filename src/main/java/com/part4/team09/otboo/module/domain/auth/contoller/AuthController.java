package com.part4.team09.otboo.module.domain.auth.contoller;

import com.part4.team09.otboo.module.common.security.constants.AuthCookieNames;
import com.part4.team09.otboo.module.common.security.jwt.GeneratedToken;
import com.part4.team09.otboo.module.common.util.CookieUtil;
import com.part4.team09.otboo.module.domain.auth.dto.ResetPasswordRequest;
import com.part4.team09.otboo.module.domain.auth.exception.AuthenticationRequiredException;
import com.part4.team09.otboo.module.domain.auth.exception.MissingRefreshTokenException;
import com.part4.team09.otboo.module.domain.auth.service.AuthService;
import com.part4.team09.otboo.module.domain.auth.service.PasswordResetService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final PasswordResetService passwordResetService;

  // csrf 토큰 조회
  @GetMapping("/csrf-token")
  public ResponseEntity<CsrfToken> getCsrfToken(CsrfToken csrfToken) {
    return ResponseEntity.ok(csrfToken);
  }

  // access 토큰 조회
  @GetMapping("/me")
  public ResponseEntity<String> getAccessToken(
    @CookieValue(name = AuthCookieNames.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
  ) {
    // refresh token 이 없는 경우 로그인 요구 예외
    if (refreshToken == null || refreshToken.isBlank()) {
      throw AuthenticationRequiredException.noDetail();
    }

    String accessToken = authService.getAccessTokenByRefreshToken(refreshToken);
    return ResponseEntity.ok(accessToken);
  }

  // refresh 토큰 재발급
  @PostMapping("/refresh")
  public ResponseEntity<String> refreshTokens(
    @CookieValue(name = AuthCookieNames.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
    HttpServletResponse response
  ) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw MissingRefreshTokenException.noDetail();
    }
    GeneratedToken generatedToken = authService.refreshTokens(refreshToken);

    Cookie refreshCookie = CookieUtil.createRefreshTokenCookie(generatedToken.refreshToken());
    response.addCookie(refreshCookie);

    return ResponseEntity.ok(generatedToken.accessToken());
  }

  // 임시 비밀번호 발급
  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    passwordResetService.resetPassword(request);
    return ResponseEntity.noContent().build();
  }
}
