package com.part4.team09.otboo.module.common.security.exception;

public class AccessTokenReplacedException extends JwtAuthenticationException {

  public AccessTokenReplacedException(String msg) {
    super(msg);
  }
}
