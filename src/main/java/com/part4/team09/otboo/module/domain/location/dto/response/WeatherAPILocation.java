package com.part4.team09.otboo.module.domain.location.dto.response;

import java.util.List;

public record WeatherAPILocation(
  double latitude,
  double longitude,
  int x,
  int y,
  List<String> locationNames
) {

}
