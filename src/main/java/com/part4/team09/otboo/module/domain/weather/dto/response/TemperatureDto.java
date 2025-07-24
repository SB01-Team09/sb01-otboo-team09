package com.part4.team09.otboo.module.domain.weather.dto.response;

public record TemperatureDto(
  double current,
  double comparedToDayBefore,
  double min,
  double max
) {

}
