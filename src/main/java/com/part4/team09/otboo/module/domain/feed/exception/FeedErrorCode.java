package com.part4.team09.otboo.module.domain.feed.exception;

import com.part4.team09.otboo.module.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum FeedErrorCode implements ErrorCode {

  FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "피드를 찾을 수 없습니다.");

  private HttpStatus httpStatus;
  private String message;

  FeedErrorCode(HttpStatus httpStatus, String message) {
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
