package com.part4.team09.otboo.module.domain.auth.exception;

import com.part4.team09.otboo.module.common.exception.ErrorCode;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

  private final ErrorCode errorCode;
  private final Map<String, Object> details;

  public AuthException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = new HashMap<>();
  }

  public AuthException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
    this.details = new HashMap<>();
  }

  public AuthException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = details;
  }

  public void addDetail(String key, Object value) {
    this.details.put(key, value);
  }
}
