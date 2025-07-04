package com.part4.team09.otboo.module.domain.weather.dto;

import com.part4.team09.otboo.module.domain.weather.entity.Humidity;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed;
import java.time.LocalDateTime;

public record WeatherData(
  Humidity humidity,
  Precipitation precipitation,
  Temperature temperature,
  WindSpeed windSpeed,
  LocalDateTime forecastedAt,
  LocalDateTime forecastAt,
  SkyStatus skyStatus,
  String locationId,
  int x,
  int y
) {

}
