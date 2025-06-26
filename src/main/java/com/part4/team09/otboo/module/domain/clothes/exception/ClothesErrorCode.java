package com.part4.team09.otboo.module.domain.clothes.exception;

import com.part4.team09.otboo.module.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ClothesErrorCode implements ErrorCode {

  // ClothesAttributeDef 에러 코드
  DUPLICATE_ATTRIBUTE_DEF_NAME(HttpStatus.CONFLICT, "중복된 의상 속성 정의 명입니다."),
  ATTRIBUTE_DEF_NOT_FOUND(HttpStatus.NOT_FOUND, "의상 속성 정의를 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String message;

  ClothesErrorCode(HttpStatus status, String message) {
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
