package com.part4.team09.otboo.module.domain.weather.controller;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.location.service.LocationService;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse.Response.Body.Items.Item;
import com.part4.team09.otboo.module.domain.weather.external.WeatherApiClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weathers")
public class WeatherController {

  private final LocationService locationService;
  private final WeatherApiClient weatherApiClient;

  @GetMapping("/location")
  public ResponseEntity<WeatherAPILocation> getLocation(
    @RequestParam("longitude") double longitude,
    @RequestParam("latitude") double latitude
  ) {
    WeatherAPILocation response = locationService.getLocation(longitude, latitude);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<Item>> getWeather() {
    return ResponseEntity.ok(weatherApiClient.getWeatherApiResponse(60, 127));
  }
}
