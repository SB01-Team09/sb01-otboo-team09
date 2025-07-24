package com.part4.team09.otboo.module.domain.recommendation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.SelectableValueRepository;
import com.part4.team09.otboo.module.domain.recommendation.dto.response.RecommendationDto;
import com.part4.team09.otboo.module.domain.recommendation.exception.RecommendationException;
import com.part4.team09.otboo.module.domain.recommendation.external.LLMApiClient;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.entity.Humidity;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation.PrecipitationType;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed.AsWord;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherNotFoundException;
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
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RecommendationInternalServiceTest {

  @Mock
  private LLMApiClient llmApiClient;
  @Mock
  private ClothesRepository clothesRepository;
  @Mock
  private HumidityRepository humidityRepository;
  @Mock
  private PrecipitationRepository precipitationRepository;
  @Mock
  private TemperatureRepository temperatureRepository;
  @Mock
  private WindSpeedRepository windSpeedRepository;
  @Mock
  private WeatherRepository weatherRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ClothesAttributeDefRepository clothesAttributeDefRepository;
  @Mock
  private SelectableValueRepository selectableValueRepository;
  @Mock
  private ClothesAttributeRepository clothesAttributeRepository;

  @InjectMocks
  private RecommendationInteranlService recommendationInteranlService;

  @Test
  void getRecommendationsByCustomAlgorithm_success() {
    // given
    Humidity humidity = Humidity.create(10, -5.0);
    UUID humidityId = UUID.randomUUID();
    ReflectionTestUtils.setField(humidity, "id", humidityId);

    Temperature temperature =
      Temperature.create(30, 3.0, 22, 30);
    UUID temperatureId = UUID.randomUUID();
    ReflectionTestUtils.setField(temperature, "id", temperatureId);

    UUID precipitationId = UUID.randomUUID();
    UUID windSpeedId = UUID.randomUUID();
    UUID coordinateId = UUID.randomUUID();

    LocalDateTime forecastAt = LocalDate.now().atTime(12, 0);
    LocalDateTime forecastedAt = LocalDate.now().atTime(12, 0);

    SkyStatus skyStatus = SkyStatus.CLEAR;

    Weather weather = Weather.create(
      forecastAt,
      forecastedAt,
      skyStatus,
      coordinateId,
      precipitationId,
      humidity.getId(),
      temperature.getId(),
      windSpeedId
    );
    UUID weatherId = UUID.randomUUID();
    ReflectionTestUtils.setField(weather, "id", weatherId);

    User user = User.createUser(
      "test@test.com",
      "user1",
      "1234"
    );
    UUID userId = UUID.randomUUID();
    ReflectionTestUtils.setField(user, "id", userId);

    List<Clothes> clothesList = List.of(Clothes.create(
      userId, "clothes1", ClothesType.TOP, "imageUrl1"
    ));

    when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(weather));
    when(humidityRepository.findById(weather.getHumidityId())).thenReturn(Optional.of(humidity));
    when(temperatureRepository.findById(weather.getTemperatureId())).thenReturn(
      Optional.of(temperature));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(clothesRepository.findAllOrderedByAttributeScores(any(), anyInt())).thenReturn(
      clothesList);
    when(clothesAttributeRepository.findAllByClothesId(any())).thenReturn(List.of());

    // when
    CompletableFuture<RecommendationDto> future =
      recommendationInteranlService.getRecommendationsByCustomAlgorithm(weatherId, userId);

    // then
    RecommendationDto dto = future.join();

    assertNotNull(dto);
    assertEquals(weatherId, dto.weatherId());
    assertEquals(userId, dto.userId());
  }

  @Test
  void getRecommendationsByCustomAlgorithm_fail_weather_not_found() {
    // given
    UUID weatherId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(weatherRepository.findById(weatherId)).thenReturn(Optional.empty());

    // when & then
    assertThrows(WeatherNotFoundException.class,
      () ->
        recommendationInteranlService.getRecommendationsByCustomAlgorithm(weatherId, userId)
          .join());
  }

  @Test
  void getRecommendationsByCustomAlgorithm_fail_user_not_found() {
    // given
    Humidity humidity = Humidity.create(10, -5.0);
    UUID humidityId = UUID.randomUUID();
    ReflectionTestUtils.setField(humidity, "id", humidityId);

    Temperature temperature =
      Temperature.create(30, 3.0, 22, 30);
    UUID temperatureId = UUID.randomUUID();
    ReflectionTestUtils.setField(temperature, "id", temperatureId);

    UUID precipitationId = UUID.randomUUID();
    UUID windSpeedId = UUID.randomUUID();
    UUID coordinateId = UUID.randomUUID();

    LocalDateTime forecastAt = LocalDate.now().atTime(12, 0);
    LocalDateTime forecastedAt = LocalDate.now().atTime(12, 0);

    SkyStatus skyStatus = SkyStatus.CLEAR;

    Weather weather = Weather.create(
      forecastAt,
      forecastedAt,
      skyStatus,
      coordinateId,
      precipitationId,
      humidity.getId(),
      temperature.getId(),
      windSpeedId
    );
    UUID weatherId = UUID.randomUUID();
    ReflectionTestUtils.setField(weather, "id", weatherId);

    UUID userId = UUID.randomUUID();

    when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(weather));
    when(humidityRepository.findById(any())).thenReturn(Optional.of(humidity));
    when(temperatureRepository.findById(any())).thenReturn(Optional.of(temperature));
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // when & then
    assertThrows(UserNotFoundException.class,
      () ->
        recommendationInteranlService.getRecommendationsByCustomAlgorithm(weatherId, userId)
          .join());
  }

  @Test
  void getRecommendationsByLLM_success() throws JsonProcessingException {
    // given
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

    UUID coordinateId = UUID.randomUUID();

    LocalDateTime forecastAt = LocalDate.now().atTime(12, 0);
    LocalDateTime forecastedAt = LocalDate.now().atTime(12, 0);

    SkyStatus skyStatus = SkyStatus.CLEAR;

    Weather weather = Weather.create(
      forecastAt,
      forecastedAt,
      skyStatus,
      coordinateId,
      precipitation.getId(),
      humidity.getId(),
      temperature.getId(),
      windSpeed.getId()
    );
    UUID weatherId = UUID.randomUUID();
    ReflectionTestUtils.setField(weather, "id", weatherId);

    User user = User.createUser(
      "test@test.com",
      "user1",
      "1234"
    );
    UUID userId = UUID.randomUUID();
    ReflectionTestUtils.setField(user, "id", userId);

    when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(weather));
    when(humidityRepository.findById(weather.getHumidityId())).thenReturn(Optional.of(humidity));
    when(temperatureRepository.findById(weather.getTemperatureId())).thenReturn(
      Optional.of(temperature));
    when(precipitationRepository.findById(weather.getPrecipitationId())).thenReturn(
      Optional.of(precipitation));
    when(windSpeedRepository.findById(weather.getWindSpeedId())).thenReturn(Optional.of(windSpeed));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(clothesAttributeDefRepository.findAll()).thenReturn(List.of());

    String json = "[{\"attribute\":\"두께\",\"values\":[\"얇음\"]}]";
    when(llmApiClient.getInfo(any())).thenReturn(json);

    List<Clothes> clothesList = List.of(Clothes.create(
      userId, "clothes1", ClothesType.TOP, "imageUrl1"
    ));
    when(clothesRepository.findAllOrderedByAttributeScores(any(), anyInt())).thenReturn(
      clothesList);
    when(clothesAttributeRepository.findAllByClothesId(any())).thenReturn(List.of());

    // when
    CompletableFuture<RecommendationDto> result =
      recommendationInteranlService.getRecommendationsByLLM(weatherId, userId);

    // then
    RecommendationDto dto = result.join();

    assertNotNull(dto);
    assertEquals(weatherId, dto.weatherId());
    assertEquals(userId, dto.userId());
  }

  @Test
  void getRecommendationsByLLM_fail_invalid_json() {
    // given
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

    UUID coordinateId = UUID.randomUUID();

    LocalDateTime forecastAt = LocalDate.now().atTime(12, 0);
    LocalDateTime forecastedAt = LocalDate.now().atTime(12, 0);

    SkyStatus skyStatus = SkyStatus.CLEAR;

    Weather weather = Weather.create(
      forecastAt,
      forecastedAt,
      skyStatus,
      coordinateId,
      precipitation.getId(),
      humidity.getId(),
      temperature.getId(),
      windSpeed.getId()
    );
    UUID weatherId = UUID.randomUUID();
    ReflectionTestUtils.setField(weather, "id", weatherId);

    User user = User.createUser(
      "test@test.com",
      "user1",
      "1234"
    );
    UUID userId = UUID.randomUUID();
    ReflectionTestUtils.setField(user, "id", userId);

    when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(weather));
    when(humidityRepository.findById(weather.getHumidityId())).thenReturn(Optional.of(humidity));
    when(temperatureRepository.findById(weather.getTemperatureId())).thenReturn(
      Optional.of(temperature));
    when(precipitationRepository.findById(weather.getPrecipitationId())).thenReturn(
      Optional.of(precipitation));
    when(windSpeedRepository.findById(weather.getWindSpeedId())).thenReturn(Optional.of(windSpeed));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(clothesAttributeDefRepository.findAll()).thenReturn(List.of());

    when(llmApiClient.getInfo(any())).thenReturn("invalid-json");

    // when & then
    assertThrows(RecommendationException.class,
      () ->
        recommendationInteranlService.getRecommendationsByLLM(weatherId, userId).join());
  }
}
