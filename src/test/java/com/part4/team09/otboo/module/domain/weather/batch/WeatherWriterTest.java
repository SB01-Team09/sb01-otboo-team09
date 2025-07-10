package com.part4.team09.otboo.module.domain.weather.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.part4.team09.otboo.config.AppConfig;
import com.part4.team09.otboo.config.MeterRegistryTestConfig;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherData;
import com.part4.team09.otboo.module.domain.weather.entity.Humidity;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation.PrecipitationType;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed.AsWord;
import com.part4.team09.otboo.module.domain.weather.repository.HumidityRepository;
import com.part4.team09.otboo.module.domain.weather.repository.PrecipitationRepository;
import com.part4.team09.otboo.module.domain.weather.repository.TemperatureRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WindSpeedRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({WeatherCache.class, WeatherWriter.class, MeterRegistryTestConfig.class, AppConfig.class})
class WeatherWriterTest {

  @Autowired
  private HumidityRepository humidityRepository;

  @Autowired
  private PrecipitationRepository precipitationRepository;

  @Autowired
  private TemperatureRepository temperatureRepository;

  @Autowired
  private WindSpeedRepository windSpeedRepository;

  @Autowired
  private WeatherRepository weatherRepository;

  @Autowired
  private WeatherCache weatherCache;

  @Autowired
  private WeatherWriter weatherWriter;

  @Test
  void write_save_test() throws Exception {
    // given
    Humidity humidity = Humidity.create(0, 0.0);
    Precipitation precipitation = Precipitation.create(PrecipitationType.NONE, 5.0, 10);
    Temperature temperature = Temperature.create(23, 5.0, 18, 31);
    WindSpeed windSpeed = WindSpeed.create(1.0, AsWord.MODERATE);
    LocalDateTime forecastedAt = LocalDateTime.now();
    LocalDateTime forecastAt = LocalDateTime.now();
    SkyStatus skyStatus = SkyStatus.CLEAR;
    String locationId = "1111111111";
    int x = 60;
    int y = 127;

    WeatherData weatherData = new WeatherData(
      humidity, precipitation, temperature, windSpeed, forecastedAt, forecastAt, skyStatus,
      locationId, x, y
    );

    List<WeatherData> weatherList = List.of(weatherData);
    Chunk<List<WeatherData>> chunk = new Chunk<>(List.of(weatherList));

    // when
    weatherWriter.write(chunk);

    // then
    List<Weather> weathers = weatherRepository.findAll();
    assertEquals(1, weathers.size());

    assertNotNull(humidityRepository.findById(weathers.get(0).getHumidityId()));
    assertNotNull(precipitationRepository.findById(weathers.get(0).getPrecipitationId()));
    assertNotNull(temperatureRepository.findById(weathers.get(0).getTemperatureId()));
    assertNotNull(windSpeedRepository.findById(weathers.get(0).getWindSpeedId()));
    assertNotNull(weatherCache.getData(x, y));
  }

  @Test
  void write_update_test() throws Exception {
    // given
    LocalDateTime forecastedAt = LocalDateTime.now();
    LocalDateTime forecastAt = LocalDateTime.now();

    setUp(forecastedAt, forecastAt);

    Humidity humidity = Humidity.create(1, 1.0);
    Precipitation precipitation = Precipitation.create(PrecipitationType.RAIN, 10.0, 60);
    Temperature temperature = Temperature.create(21, 6.0, 20, 34);
    WindSpeed windSpeed = WindSpeed.create(2.0, AsWord.STRONG);
    SkyStatus skyStatus = SkyStatus.CLOUDY;
    String locationId = "1111111112";
    int x = 60;
    int y = 127;

    WeatherData weatherData = new WeatherData(
      humidity, precipitation, temperature, windSpeed, forecastedAt, forecastAt, skyStatus,
      locationId, x, y
    );

    List<WeatherData> weatherList = List.of(weatherData);
    Chunk<List<WeatherData>> chunk = new Chunk<>(List.of(weatherList));

    // when
    weatherWriter.write(chunk);

    // then
    List<Weather> weathers = weatherRepository.findAll();
//    assertEquals(1, weathers.size());

    Humidity savedHumidity = humidityRepository.findById(weathers.get(0).getHumidityId()).get();
    assertNotNull(savedHumidity);
    assertEquals(1, savedHumidity.getCurrent());

    assertNotNull(precipitationRepository.findById(weathers.get(0).getPrecipitationId()));
    assertNotNull(temperatureRepository.findById(weathers.get(0).getTemperatureId()));
    assertNotNull(windSpeedRepository.findById(weathers.get(0).getWindSpeedId()));
    assertNotNull(weatherCache.getData(x, y));
  }

  private void setUp(LocalDateTime forecastedAt, LocalDateTime forecastAt) {
    Humidity humidity = Humidity.create(0, 0.0);
    Precipitation precipitation = Precipitation.create(PrecipitationType.NONE, 5.0, 10);
    Temperature temperature = Temperature.create(23, 5.0, 18, 31);
    WindSpeed windSpeed = WindSpeed.create(1.0, AsWord.MODERATE);
    SkyStatus skyStatus = SkyStatus.CLEAR;
    String locationId = "1111111112";
    int x = 60;
    int y = 127;

    Humidity savedHumidity = humidityRepository.save(humidity);
    Precipitation savedPrecipitation = precipitationRepository.save(precipitation);
    Temperature savedTemperature = temperatureRepository.save(temperature);
    WindSpeed savedWindSpeed = windSpeedRepository.save(windSpeed);
    weatherRepository.save(Weather.create(
      forecastAt, forecastedAt, skyStatus, locationId,
      savedPrecipitation.getId(), savedHumidity.getId(), savedTemperature.getId(),
      savedWindSpeed.getId()
    ));
  }

}