package com.part4.team09.otboo.module.domain.weather.exception;

public class WeatherReadException extends WeatherException {

  public WeatherReadException(WeatherErrorCode errorCode) {
    super(errorCode);
  }

  public static WeatherReadException withId(Object id) {
    WeatherReadException exception =
      new WeatherReadException(WeatherErrorCode.WEATHER_DATA_FETCH_FAILED);
    exception.addDetail("id", id);
    return exception;
  }
}
