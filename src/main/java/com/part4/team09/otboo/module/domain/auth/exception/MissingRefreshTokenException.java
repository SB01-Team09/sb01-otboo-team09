package com.part4.team09.otboo.module.domain.auth.exception;

public class MissingRefreshTokenException extends AuthException {

  public MissingRefreshTokenException() {
    super(AuthErrorCode.REFRESH_TOKEN_MISSING);
  }

  public static MissingRefreshTokenException noDetail() {
    return new MissingRefreshTokenException();
  }
}
