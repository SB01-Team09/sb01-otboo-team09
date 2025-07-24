package com.part4.team09.otboo.module.domain.location.exception;

import com.part4.team09.otboo.module.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum LocationErrorCode implements ErrorCode {

  LOCATION_NOF_FOUND(HttpStatus.NOT_FOUND, "현재 위치에 대한 지역 정보를 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String message;

  LocationErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  @Override
  public HttpStatus getHttpStatus() {
    return this.status;
  }

  @Override
  public String getMessage() {
    return this.message;
  }
}
