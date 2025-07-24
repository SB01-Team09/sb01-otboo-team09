package com.part4.team09.otboo.module.domain.weather.dto.response;

import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed.AsWord;

public record WindSpeedDto(
  double speed,
  AsWord asWord
) {

}
