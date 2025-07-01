package com.part4.team09.otboo.module.domain.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.location.service.LocationService;
import com.part4.team09.otboo.module.domain.weather.dto.response.HumidityDto;
import com.part4.team09.otboo.module.domain.weather.dto.response.PrecipitationDto;
import com.part4.team09.otboo.module.domain.weather.dto.response.TemperatureDto;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherDto;
import com.part4.team09.otboo.module.domain.weather.dto.response.WindSpeedDto;
import com.part4.team09.otboo.module.domain.weather.entity.Humidity;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation.PrecipitationType;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed.AsWord;
import com.part4.team09.otboo.module.domain.weather.mapper.WeatherMapper;
import com.part4.team09.otboo.module.domain.weather.repository.HumidityRepository;
import com.part4.team09.otboo.module.domain.weather.repository.PrecipitationRepository;
import com.part4.team09.otboo.module.domain.weather.repository.TemperatureRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WindSpeedRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

  @Mock
  private LocationService locationService;
  @Mock
  private WeatherRepository weatherRepository;
  @Mock
  private HumidityRepository humidityRepository;
  @Mock
  private PrecipitationRepository precipitationRepository;
  @Mock
  private TemperatureRepository temperatureRepository;
  @Mock
  private WindSpeedRepository windSpeedRepository;
  @Spy
  private WeatherMapper weatherMapper;

  @InjectMocks
  private WeatherService weatherService;

  @Test
  void getWeather_byCoordinates_success() {
    // given
    double longitude = 127.0;
    double latitude = 37.0;
    int x = 60;
    int y = 127;

    WeatherAPILocation location = new WeatherAPILocation(
      latitude,
      longitude,
      x,
      y,
      List.of("서울특별시", "종로", "청운효자동")
    );

    Humidity humidity = Humidity.create(10, -5.0);
    UUID humidityId = UUID.randomUUID();
    ReflectionTestUtils.setField(humidity, "id", humidityId);

    Precipitation precipitation =
      Precipitation.create(PrecipitationType.of(1), 1, 60);
    UUID precipitationId = UUID.randomUUID();
    ReflectionTestUtils.setField(precipitation, "id", precipitationId);

    Temperature temperature =
      Temperature.create(30, 3.0, 22, 30);
    UUID temperatureId = UUID.randomUUID();
    ReflectionTestUtils.setField(temperature, "id", temperatureId);

    WindSpeed windSpeed = WindSpeed.create(8, AsWord.fromSpeed(8));
    UUID windSpeedId = UUID.randomUUID();
    ReflectionTestUtils.setField(windSpeed, "id", windSpeedId);

    String locationId = "1111051500";
    LocalDateTime forecastAt = LocalDate.now().atTime(12, 0);
    LocalDateTime forecastedAt = LocalDate.now().atTime(12, 0);

    SkyStatus skyStatus = SkyStatus.CLEAR;

    Weather weather = Weather.create(
      forecastAt,
      forecastedAt,
      skyStatus,
      locationId,
      precipitation.getId(),
      humidity.getId(),
      temperature.getId(),
      windSpeed.getId()
    );
    UUID weatherId = UUID.randomUUID();
    ReflectionTestUtils.setField(weather, "id", weatherId);

    HumidityDto humidityDto = weatherMapper.toHumidityDto(humidity);
    PrecipitationDto precipitationDto = weatherMapper.toPrecipitationDto(precipitation);
    TemperatureDto temperatureDto = weatherMapper.toTemperatureDto(temperature);
    WindSpeedDto windSpeedDto = weatherMapper.toWindSpeedDto(windSpeed);

    WeatherDto expectedDto = weatherMapper.toWeatherDto(
      weatherId,
      forecastedAt,
      forecastAt,
      location,
      skyStatus,
      precipitationDto,
      humidityDto,
      temperatureDto,
      windSpeedDto
    );

    when(locationService.getLocationCodeByCoordinates(longitude, latitude)).thenReturn(locationId);
    when(locationService.getLocation(locationId)).thenReturn(location);
    when(weatherRepository.findByLocationIdAndForecastAtGreaterThanEqual(locationId, forecastAt))
      .thenReturn(List.of(weather));
    when(humidityRepository.findById(any())).thenReturn(Optional.of(humidity));
    when(precipitationRepository.findById(any())).thenReturn(Optional.of(precipitation));
    when(temperatureRepository.findById(any())).thenReturn(Optional.of(temperature));
    when(windSpeedRepository.findById(any())).thenReturn(Optional.of(windSpeed));

    // when
    List<WeatherDto> result = weatherService.getWeather(longitude, latitude);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(expectedDto);

    verify(locationService).getLocationCodeByCoordinates(longitude, latitude);
    verify(weatherRepository).findByLocationIdAndForecastAtGreaterThanEqual(locationId, forecastAt);
  }
}