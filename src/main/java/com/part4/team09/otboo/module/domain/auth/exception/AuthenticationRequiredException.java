package com.part4.team09.otboo.module.domain.auth.exception;

public class AuthenticationRequiredException extends AuthException {

  public AuthenticationRequiredException() {
    super(AuthErrorCode.AUTHENTICATION_REQUIRED);
  }

  public static AuthenticationRequiredException noDetail() {
    return new AuthenticationRequiredException();
  }
}
