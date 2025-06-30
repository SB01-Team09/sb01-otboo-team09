package com.part4.team09.otboo.module.domain.weather.service;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.location.service.LocationService;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherDto;
import com.part4.team09.otboo.module.domain.weather.entity.Humidity;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed;
import com.part4.team09.otboo.module.domain.weather.mapper.WeatherMapper;
import com.part4.team09.otboo.module.domain.weather.repository.HumidityRepository;
import com.part4.team09.otboo.module.domain.weather.repository.PrecipitationRepository;
import com.part4.team09.otboo.module.domain.weather.repository.TemperatureRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WindSpeedRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherService {

  private final LocationService locationService;
  private final WeatherRepository weatherRepository;
  private final HumidityRepository humidityRepository;
  private final PrecipitationRepository precipitationRepository;
  private final TemperatureRepository temperatureRepository;
  private final WindSpeedRepository windSpeedRepository;
  private final WeatherMapper weatherMapper;

  public final List<WeatherDto> getWeather(double longitude, double latitude) {
    String locationId = locationService.getLocationCodeByCoordinates(longitude, latitude);
    WeatherAPILocation location = locationService.getLocation(locationId);

    LocalDateTime forecastAt = LocalDate.now().atTime(12, 0);
    List<Weather> weathers = weatherRepository
      .findByLocationIdAndForecastAtGreaterThanEqual(locationId, forecastAt);

    List<WeatherDto> weatherDtos = weathers.stream()
      .map(weather -> {
        Humidity humidity = humidityRepository.findById(weather.getHumidityId())
          .orElseThrow();

        Precipitation precipitation = precipitationRepository.findById(weather.getPrecipitationId())
          .orElseThrow();

        Temperature temperature = temperatureRepository.findById(weather.getTemperatureId())
          .orElseThrow();

        WindSpeed windSpeed = windSpeedRepository.findById(weather.getWindSpeedId())
          .orElseThrow();

        WeatherDto weatherDto = weatherMapper.toWeatherDto(
          weather.getId(),
          weather.getForecastedAt(),
          weather.getForecastAt(),
          location,
          weather.getSkyStatus(),
          weatherMapper.toPrecipitationDto(precipitation),
          weatherMapper.toHumidityDto(humidity),
          weatherMapper.toTemperatureDto(temperature),
          weatherMapper.toWindSpeedDto(windSpeed)
        );

        return weatherDto;
      })
      .toList();

    return weatherDtos;
  }

}
