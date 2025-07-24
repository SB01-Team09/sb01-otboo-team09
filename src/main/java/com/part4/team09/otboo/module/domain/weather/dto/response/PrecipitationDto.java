package com.part4.team09.otboo.module.domain.weather.dto.response;

import com.part4.team09.otboo.module.domain.weather.entity.Precipitation.PrecipitationType;

public record PrecipitationDto(
  PrecipitationType type,
  double amount,
  double probability
) {

}
