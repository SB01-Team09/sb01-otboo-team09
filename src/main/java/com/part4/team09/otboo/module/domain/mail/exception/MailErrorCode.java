package com.part4.team09.otboo.module.domain.mail.exception;

import com.part4.team09.otboo.module.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum MailErrorCode implements ErrorCode {
  ENCODING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "메일 주소 인코딩에 실패했습니다."),
  MESSAGING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "메일 전송 중 메시지 구성이 실패했습니다."),
  UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR, "메일 전송 중 알 수 없는 오류가 발생했습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  MailErrorCode(HttpStatus httpStatus, String message) {
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
