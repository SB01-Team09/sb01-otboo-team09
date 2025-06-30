package com.part4.team09.otboo.module.domain.weather.mapper;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.weather.dto.response.HumidityDto;
import com.part4.team09.otboo.module.domain.weather.dto.response.PrecipitationDto;
import com.part4.team09.otboo.module.domain.weather.dto.response.TemperatureDto;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherDto;
import com.part4.team09.otboo.module.domain.weather.dto.response.WindSpeedDto;
import com.part4.team09.otboo.module.domain.weather.entity.Humidity;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed;
import java.time.LocalDateTime;
import java.util.UUID;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WeatherMapper {

  WeatherDto toWeatherDto(
    UUID id,
    LocalDateTime forecastedAt,
    LocalDateTime forecasteAt,
    WeatherAPILocation location,
    SkyStatus skyStatus,
    PrecipitationDto precipitation,
    HumidityDto humidity,
    TemperatureDto temperature,
    WindSpeedDto windSpeed
  );

  HumidityDto toHumidityDto(Humidity humidity);

  PrecipitationDto toPrecipitationDto(Precipitation precipitation);

  TemperatureDto toTemperatureDto(Temperature temperature);

  WindSpeedDto toWindSpeedDto(WindSpeed windSpeed);

}
