package com.part4.team09.otboo.module.domain.user.exception;

import com.part4.team09.otboo.module.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum UserErrorCode implements ErrorCode {

  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
  EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다.");

  private HttpStatus httpStatus;
  private String message;

  UserErrorCode(HttpStatus httpStatus, String message) {
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
