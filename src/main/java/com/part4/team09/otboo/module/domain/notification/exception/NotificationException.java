package com.part4.team09.otboo.module.domain.notification.exception;

import com.part4.team09.otboo.module.common.exception.BaseException;
import com.part4.team09.otboo.module.common.exception.ErrorCode;
import java.util.Map;

public class NotificationException extends BaseException {

  public NotificationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public NotificationException(String message, ErrorCode errorCode) {
    super(message, errorCode);
  }

  public NotificationException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
