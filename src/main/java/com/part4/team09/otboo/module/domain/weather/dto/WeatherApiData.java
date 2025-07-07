package com.part4.team09.otboo.module.domain.weather.dto;

import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse.Response.Body.Items.Item;
import java.util.List;

public record WeatherApiData(
  String locationId,
  List<Item> items,
  int x,
  int y
) {

}
