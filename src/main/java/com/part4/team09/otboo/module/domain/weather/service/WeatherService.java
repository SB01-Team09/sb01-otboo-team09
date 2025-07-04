package com.part4.team09.otboo.module.domain.weather.service;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.location.service.LocationService;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherDto;
import com.part4.team09.otboo.module.domain.weather.entity.Humidity;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherErrorCode;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherNotFoundException;
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
    // 지역 정보 조회 및 dto 변환
    String locationId = locationService.getLocationCodeByCoordinates(longitude, latitude);
    WeatherAPILocation location = locationService.getLocation(locationId);

    // 지역에 해당하는 날씨 정보 리스트 조회 및 dto 변환
    LocalDateTime forecastAt = LocalDate.now().atTime(12, 0);
    List<Weather> weathers = weatherRepository
      .findByLocationIdAndForecastAtGreaterThanEqual(locationId, forecastAt);

    List<WeatherDto> weatherDtos = weathers.stream()
      .map(weather -> {
        Humidity humidity = humidityRepository.findById(weather.getHumidityId())
          .orElseThrow(() -> WeatherNotFoundException
            .withId(WeatherErrorCode.HUMIDITY_NOF_FOUND, weather.getHumidityId()));

        Precipitation precipitation = precipitationRepository.findById(weather.getPrecipitationId())
          .orElseThrow(() -> WeatherNotFoundException
            .withId(WeatherErrorCode.PRECIPITATION_NOF_FOUND, weather.getPrecipitationId()));

        Temperature temperature = temperatureRepository.findById(weather.getTemperatureId())
          .orElseThrow(() -> WeatherNotFoundException
            .withId(WeatherErrorCode.TEMPERATURE_NOF_FOUND, weather.getTemperatureId()));

        WindSpeed windSpeed = windSpeedRepository.findById(weather.getWindSpeedId())
          .orElseThrow(() -> WeatherNotFoundException
            .withId(WeatherErrorCode.WINDSPEED_NOF_FOUND, weather.getWindSpeedId()));

        return weatherMapper.toWeatherDto(
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
      })
      .toList();

    return weatherDtos;
  }

}
