package com.part4.team09.otboo.module.domain.weather.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.location.service.LocationService;
import com.part4.team09.otboo.module.domain.weather.dto.response.HumidityDto;
import com.part4.team09.otboo.module.domain.weather.dto.response.PrecipitationDto;
import com.part4.team09.otboo.module.domain.weather.dto.response.TemperatureDto;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherDto;
import com.part4.team09.otboo.module.domain.weather.dto.response.WindSpeedDto;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation.PrecipitationType;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed.AsWord;
import com.part4.team09.otboo.module.domain.weather.service.WeatherService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class WeatherControllerTest {

  private MockMvc mockMvc;

  @Mock
  private LocationService locationService;

  @Mock
  private WeatherService weatherService;

  @InjectMocks
  private WeatherController weatherController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(weatherController).build();
  }

  @Test
  void getLocationTest() throws Exception {
    double longitude = 127.0;
    double latitude = 37.5;

    WeatherAPILocation mockLocation = new WeatherAPILocation(
      latitude,
      longitude,
      60,
      127,
      List.of("서울특별시", "강남구", "삼성동")
    );

    given(locationService.getLocation(longitude, latitude)).willReturn(mockLocation);

    mockMvc.perform(MockMvcRequestBuilders.get("/api/weathers/location")
        .param("longitude", String.valueOf(longitude))
        .param("latitude", String.valueOf(latitude)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.latitude").value(latitude))
      .andExpect(jsonPath("$.longitude").value(longitude))
      .andExpect(jsonPath("$.x").value(60))
      .andExpect(jsonPath("$.y").value(127))
      .andExpect(jsonPath("$.locationNames[0]").value("서울특별시"));
  }

  @Test
  void getWeatherTest() throws Exception {
    double longitude = 127.0;
    double latitude = 37.5;

    WeatherAPILocation location = new WeatherAPILocation(
      latitude, longitude, 60, 127, List.of("서울특별시", "강남구", "삼성동")
    );

    WeatherDto weatherDto = new WeatherDto(
      UUID.randomUUID(),
      LocalDateTime.of(2025, 7, 28, 12, 0),
      LocalDateTime.of(2025, 7, 28, 6, 0),
      location,
      SkyStatus.CLEAR,
      new PrecipitationDto(PrecipitationType.NONE, 0.0, 0.0),
      new HumidityDto(55.0, 3.0),
      new TemperatureDto(29.5, 1.5, 25.0, 32.0),
      new WindSpeedDto(2.5, AsWord.STRONG)
    );

    given(weatherService.getWeather(longitude, latitude))
      .willReturn(List.of(weatherDto));

    mockMvc.perform(MockMvcRequestBuilders.get("/api/weathers")
        .param("longitude", String.valueOf(longitude))
        .param("latitude", String.valueOf(latitude)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].location.x").value(60))
      .andExpect(jsonPath("$[0].skyStatus").value("CLEAR"))
      .andExpect(jsonPath("$[0].temperature.current").value(29.5))
      .andExpect(jsonPath("$[0].windSpeed.speed").value(2.5));
  }
}
