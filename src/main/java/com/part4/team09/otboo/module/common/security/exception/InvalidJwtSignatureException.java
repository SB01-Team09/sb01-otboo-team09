package com.part4.team09.otboo.module.common.security.exception;

/**
 * 서명 불일치
 */
public class InvalidJwtSignatureException extends JwtAuthenticationException {

  public InvalidJwtSignatureException(String msg) {
    super(msg);
  }
}
