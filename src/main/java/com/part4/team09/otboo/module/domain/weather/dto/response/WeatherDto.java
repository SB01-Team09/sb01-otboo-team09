package com.part4.team09.otboo.module.domain.weather.dto.response;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record WeatherDto(
  UUID id,
  LocalDateTime forecastedAt,
  LocalDateTime forecasteAt,
  WeatherAPILocation location,
  SkyStatus skyStatus,
  PrecipitationDto precipitation,
  HumidityDto humidity,
  TemperatureDto temperature,
  WindSpeedDto windSpeed
) {

}
