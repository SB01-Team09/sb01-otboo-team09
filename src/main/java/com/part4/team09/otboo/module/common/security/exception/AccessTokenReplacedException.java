package com.part4.team09.otboo.module.common.security.exception;

/**
 * 이전 토큰으로 인증 시도
 */
public class AccessTokenReplacedException extends JwtAuthenticationException {

  public AccessTokenReplacedException(String msg) {
    super(msg);
  }
}
