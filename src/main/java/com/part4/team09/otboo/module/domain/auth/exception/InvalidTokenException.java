package com.part4.team09.otboo.module.domain.auth.exception;

public class InvalidTokenException extends AuthException {

  public InvalidTokenException() {
    super(AuthErrorCode.INVALID_TOKEN);
  }

  public static InvalidTokenException noDetail() {
    return new InvalidTokenException();
  }
}
