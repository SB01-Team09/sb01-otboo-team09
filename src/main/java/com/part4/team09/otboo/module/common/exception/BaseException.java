package com.part4.team09.otboo.module.common.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

  private final ErrorCode errorCode;
  private final Map<String, Object> details;

  public BaseException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = new HashMap<>();
  }

  public BaseException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
    this.details = new HashMap<>();
  }

  public BaseException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = details;
  }

  public BaseException addDetail(String key, Object value) {
    this.details.put(key, value);
    return this;
  }
}
