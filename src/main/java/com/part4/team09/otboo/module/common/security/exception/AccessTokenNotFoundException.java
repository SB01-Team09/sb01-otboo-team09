package com.part4.team09.otboo.module.common.security.exception;

public class AccessTokenNotFoundException extends JwtAuthenticationException {

  public AccessTokenNotFoundException(String msg) {
    super(msg);
  }
}
