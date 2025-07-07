package com.part4.team09.otboo.module.domain.auth.contoller;

import com.part4.team09.otboo.module.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  // csrf 토큰 조회
  @RequestMapping("/csrf-token")
  public ResponseEntity<CsrfToken> getCsrfToken(CsrfToken csrfToken) {
    return ResponseEntity.ok(csrfToken);
  }

  // access 토큰 조회

  // refresh 토큰 재발급

  // 비밀번호 초기화

}
