package com.part4.team09.otboo.module.domain.weather.exception;

public class WeatherNotFoundException extends WeatherException {

  public WeatherNotFoundException(WeatherErrorCode errorCode) {
    super(errorCode);
  }

  public static WeatherNotFoundException withId(WeatherErrorCode errorCode, Object id) {
    WeatherNotFoundException exception = new WeatherNotFoundException(errorCode);
    exception.addDetail("id", id);
    return exception;
  }
}
