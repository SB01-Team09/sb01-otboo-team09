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
import java.util.UUID;
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
  private final WeatherCache weatherCache;

  @Override
  public void write(Chunk<? extends WeatherData> weatherDatas) throws Exception {
    for (WeatherData weatherData : weatherDatas) {
      weatherRepository
        .findByLocationIdAndForecastAt(weatherData.locationId(), weatherData.forecastAt())
        .ifPresentOrElse(
          existingWeather -> updateExistingWeather(existingWeather, weatherData),
          () -> saveNewWeather(weatherData)
        );
    }
  }

  private void updateExistingWeather(Weather existingWeather, WeatherData weatherData) {
    updateHumidity(existingWeather.getHumidityId(), weatherData.humidity());
    updatePrecipitation(existingWeather.getPrecipitationId(), weatherData.precipitation());
    updateTemperature(existingWeather.getTemperatureId(), weatherData.temperature());
    updateWindSpeed(existingWeather.getWindSpeedId(), weatherData.windSpeed());

    weatherCache
      .putData(weatherData.x(), weatherData.y(), existingWeather.getForecastAt(), existingWeather);
  }

  private void updateHumidity(UUID humidityId, Humidity newHumidity) {
    humidityRepository.findById(humidityId).ifPresent(humidity -> {
      humidity.updateCurrent(newHumidity.getCurrent());
      humidity.updateComparedToDayBefore(newHumidity.getComparedToDayBefore());
    });
  }

  private void updatePrecipitation(UUID precipitationId, Precipitation newPrecipitation) {
    precipitationRepository.findById(precipitationId).ifPresent(precipitation -> {
      precipitation.updateType(newPrecipitation.getType());
      precipitation.updateAmount(newPrecipitation.getAmount());
      precipitation.updateProbability(newPrecipitation.getProbability());
    });
  }

  private void updateTemperature(UUID temperatureId, Temperature newTemperature) {
    temperatureRepository.findById(temperatureId).ifPresent(temperature -> {
      temperature.updateCurrent(newTemperature.getCurrent());
      temperature.updateComparedToDayBefore(newTemperature.getComparedToDayBefore());
      temperature.updateMax(newTemperature.getMax());
      temperature.updateMin(newTemperature.getMin());
    });
  }

  private void updateWindSpeed(UUID windSpeedId, WindSpeed newWindSpeed) {
    windSpeedRepository.findById(windSpeedId).ifPresent(windSpeed -> {
      windSpeed.updateSpeed(newWindSpeed.getSpeed());
      windSpeed.updateAsWord(newWindSpeed.getAsWord());
    });
  }

  private void saveNewWeather(WeatherData weatherData) {
    humidityRepository.save(weatherData.humidity());
    precipitationRepository.save(weatherData.precipitation());
    temperatureRepository.save(weatherData.temperature());
    windSpeedRepository.save(weatherData.windSpeed());

    Weather newWeather = Weather.create(
      weatherData.forecastAt(),
      weatherData.forecastedAt(),
      weatherData.skyStatus(),
      weatherData.locationId(),
      weatherData.precipitation().getId(),
      weatherData.humidity().getId(),
      weatherData.temperature().getId(),
      weatherData.windSpeed().getId()
    );

    Weather savedWeather = weatherRepository.save(newWeather);

    weatherCache
      .putData(weatherData.x(), weatherData.y(), savedWeather.getForecastAt(), savedWeather);
  }
}
