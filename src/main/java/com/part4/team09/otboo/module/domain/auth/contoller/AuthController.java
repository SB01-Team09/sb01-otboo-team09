package com.part4.team09.otboo.module.domain.auth.contoller;

import com.part4.team09.otboo.module.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  // csrf 토큰 조회

  // 액세스 토큰 조회

  // 토큰 재발급

  // 비밀번호 초기화

}
