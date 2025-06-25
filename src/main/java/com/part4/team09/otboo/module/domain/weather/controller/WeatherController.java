package com.part4.team09.otboo.module.domain.weather.controller;

import com.part4.team09.otboo.module.domain.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weathers")
public class WeatherController {

  private final LocationService locationService;

  @GetMapping("/location")
  public ResponseEntity<String> getLocation() {
    double x = 127.10459896729914;
    double y = 37.40269721785548;
    locationService.createLocation(x, y);
    return null;
  }

}
