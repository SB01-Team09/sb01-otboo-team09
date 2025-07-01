package com.part4.team09.otboo.module.domain.user.exception;

import com.part4.team09.otboo.module.common.exception.BaseException;
import com.part4.team09.otboo.module.common.exception.ErrorCode;
import java.util.Map;

public class UserException extends BaseException {

  public UserException(ErrorCode errorCode) {
    super(errorCode);
  }

  public UserException(String message, ErrorCode errorCode) {
    super(message, errorCode);
  }

  public UserException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
