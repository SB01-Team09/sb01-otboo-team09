package com.part4.team09.otboo.module.domain.auth.exception;

import com.part4.team09.otboo.module.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
  ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "계정이 잠겼습니다. 관리자에게 문의하세요."),
  CREDENTIALS_EXPIRED(HttpStatus.UNAUTHORIZED, "비밀번호가 만료되었습니다."),
  AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  AuthErrorCode(HttpStatus httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
  }

  @Override
  public HttpStatus getHttpStatus() {
    return this.httpStatus;
  }

  @Override
  public String getMessage() {
    return this.message;
  }
}
