package com.part4.team09.otboo.module.domain.auth.exception;

/**
 * 토큰 만료 예외
 */
public class JwtExpiredException extends JwtAuthenticationException {

  public JwtExpiredException(String msg) {
    super(msg);
  }
}
