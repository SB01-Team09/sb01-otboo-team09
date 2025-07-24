package com.part4.team09.otboo.module.domain.location.exception;

import com.part4.team09.otboo.module.common.exception.BaseException;
import com.part4.team09.otboo.module.common.exception.ErrorCode;
import java.util.Map;

public class LocationException extends BaseException {

  public LocationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public LocationException(String message, ErrorCode errorCode) {
    super(message, errorCode);
  }

  public LocationException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
