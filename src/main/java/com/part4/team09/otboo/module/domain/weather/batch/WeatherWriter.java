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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherWriter implements ItemWriter<List<WeatherData>> {

  private final HumidityRepository humidityRepository;
  private final PrecipitationRepository precipitationRepository;
  private final TemperatureRepository temperatureRepository;
  private final WindSpeedRepository windSpeedRepository;
  private final WeatherRepository weatherRepository;
  private final WeatherCache weatherCache;

  @Override
  public void write(Chunk<? extends List<WeatherData>> chunk) throws Exception {
    chunk.forEach(
      weatherDatas -> {
        List<Weather> weathers = new ArrayList<>();
        weatherDatas.forEach(
          weatherData -> {
            Weather savedWeather = weatherRepository
              .findByLocationIdAndForecastAt(weatherData.locationId(), weatherData.forecastAt())
              .map(
                weather -> updateExistingWeather(weather, weatherData)
              )
              .orElseGet(() -> saveNewWeather(weatherData));

            weathers.add(savedWeather);
          }
        );

        weatherCache.putData(weatherDatas.get(0).x(), weatherDatas.get(0).y(), weathers);
      }
    );
  }

  private Weather updateExistingWeather(Weather existingWeather, WeatherData weatherData) {
    updateHumidity(existingWeather.getHumidityId(), weatherData.humidity());
    updatePrecipitation(existingWeather.getPrecipitationId(), weatherData.precipitation());
    updateTemperature(existingWeather.getTemperatureId(), weatherData.temperature());
    updateWindSpeed(existingWeather.getWindSpeedId(), weatherData.windSpeed());

    return existingWeather;
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

  private Weather saveNewWeather(WeatherData weatherData) {
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

    return weatherRepository.save(newWeather);
  }
}
