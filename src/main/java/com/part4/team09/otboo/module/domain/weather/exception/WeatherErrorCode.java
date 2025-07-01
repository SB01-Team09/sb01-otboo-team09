package com.part4.team09.otboo.module.domain.weather.exception;

import com.part4.team09.otboo.module.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum WeatherErrorCode implements ErrorCode {
  HUMIDITY_NOF_FOUND(HttpStatus.NOT_FOUND, "습도 정보를 찾을 수 없습니다."),
  PRECIPITATION_NOF_FOUND(HttpStatus.NOT_FOUND, "강수량 정보를 찾을 수 없습니다."),
  TEMPERATURE_NOF_FOUND(HttpStatus.NOT_FOUND, "온도 정보를 찾을 수 없습니다."),
  WINDSPEED_NOF_FOUND(HttpStatus.NOT_FOUND, "풍속 정보를 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String message;

  WeatherErrorCode(HttpStatus status, String message) {
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
