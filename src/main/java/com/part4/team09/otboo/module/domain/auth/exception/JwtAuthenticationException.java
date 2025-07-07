package com.part4.team09.otboo.module.domain.auth.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * jwt 인증 예외
 */
public class JwtAuthenticationException extends AuthenticationException {

  public JwtAuthenticationException(String msg) {
    super(msg);
  }

  public JwtAuthenticationException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
