package com.part4.team09.otboo.module.common.security.exception;

/**
 * 해당 인증 토큰을 찾을 수 없음
 */
public class AccessTokenNotFoundException extends JwtAuthenticationException {

  public AccessTokenNotFoundException(String msg) {
    super(msg);
  }
}
