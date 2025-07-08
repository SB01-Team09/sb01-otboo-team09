package com.part4.team09.otboo.module.common.security.exception;

/**
 * 형식이 잘못된 jwt
 */
public class InvalidJwtFormatException extends JwtAuthenticationException {

  public InvalidJwtFormatException(String msg) {
    super(msg);
  }
}
