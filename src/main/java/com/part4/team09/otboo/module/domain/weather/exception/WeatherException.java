package com.part4.team09.otboo.module.domain.weather.exception;

import com.part4.team09.otboo.module.common.exception.BaseException;
import com.part4.team09.otboo.module.common.exception.ErrorCode;
import java.util.Map;

public class WeatherException extends BaseException {

  public WeatherException(ErrorCode errorCode) {
    super(errorCode);
  }

  public WeatherException(String message, ErrorCode errorCode) {
    super(message, errorCode);
  }

  public WeatherException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
