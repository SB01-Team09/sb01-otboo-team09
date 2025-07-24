package com.part4.team09.otboo.module.domain.feed.exception;

import com.part4.team09.otboo.module.common.exception.BaseException;
import com.part4.team09.otboo.module.common.exception.ErrorCode;
import java.util.Map;

public class FeedException extends BaseException {

  public FeedException(ErrorCode errorCode) {
    super(errorCode);
  }

  public FeedException(String message, ErrorCode errorCode) {
    super(message, errorCode);
  }

  public FeedException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
