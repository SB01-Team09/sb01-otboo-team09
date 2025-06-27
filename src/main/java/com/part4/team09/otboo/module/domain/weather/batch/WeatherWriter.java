package com.part4.team09.otboo.module.domain.weather.batch;

import com.part4.team09.otboo.module.domain.weather.dto.WeatherData;
import com.part4.team09.otboo.module.domain.weather.entity.Humidity;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed;
import com.part4.team09.otboo.module.domain.weather.repository.HumidityRepository;
import com.part4.team09.otboo.module.domain.weather.repository.PrecipitationRepository;
import com.part4.team09.otboo.module.domain.weather.repository.TemperatureRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WindSpeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherWriter implements ItemWriter<WeatherData> {

  private final HumidityRepository humidityRepository;
  private final PrecipitationRepository precipitationRepository;
  private final TemperatureRepository temperatureRepository;
  private final WindSpeedRepository windSpeedRepository;
  private final WeatherRepository weatherRepository;

  @Override
  public void write(Chunk<? extends WeatherData> weatherDatas) throws Exception {
    weatherDatas.forEach(
      weatherData -> {
        Humidity humidity = humidityRepository.save(weatherData.humidity());
        Precipitation precipitation = precipitationRepository.save(weatherData.precipitation());
        Temperature temperature = temperatureRepository.save(weatherData.temperature());
        WindSpeed windSpeed = windSpeedRepository.save(weatherData.windSpeed());

        Weather weather = Weather.create(
          weatherData.forecastAt(),
          weatherData.forecastedAt(),
          weatherData.skyStatus(),
          weatherData.locationId(),
          precipitation.getId(),
          humidity.getId(),
          temperature.getId(),
          windSpeed.getId()
        );
        weatherRepository.save(weather);
      }
    );
  }
}
