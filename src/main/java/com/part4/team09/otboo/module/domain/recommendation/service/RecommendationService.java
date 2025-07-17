package com.part4.team09.otboo.module.domain.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.exception.SelectableValue.SelectableValueNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.SelectableValueRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.custom.ClothesRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.recommendation.dto.ClothingOption;
import com.part4.team09.otboo.module.domain.recommendation.dto.response.RecommendationClothesAttributeDto;
import com.part4.team09.otboo.module.domain.recommendation.dto.response.RecommendationClothesDto;
import com.part4.team09.otboo.module.domain.recommendation.dto.response.RecommendationDto;
import com.part4.team09.otboo.module.domain.recommendation.external.LLMApiClient;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.entity.Humidity;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherErrorCode;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherNotFoundException;
import com.part4.team09.otboo.module.domain.weather.repository.HumidityRepository;
import com.part4.team09.otboo.module.domain.weather.repository.PrecipitationRepository;
import com.part4.team09.otboo.module.domain.weather.repository.TemperatureRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WindSpeedRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationService {

  private final LLMApiClient llmApiClient;
  private final ClothesRepositoryQueryDSL clothesRepositoryQueryDSL;
  private final HumidityRepository humidityRepository;
  private final PrecipitationRepository precipitationRepository;
  private final TemperatureRepository temperatureRepository;
  private final WindSpeedRepository windSpeedRepository;
  private final WeatherRepository weatherRepository;
  private final UserRepository userRepository;
  private final ClothesAttributeDefRepository clothesAttributeDefRepository;
  private final SelectableValueRepository selectableValueRepository;
  private final ClothesAttributeRepository clothesAttributeRepository;

  public RecommendationDto getRecommendationsByLLM(UUID weatherId, UUID userId) {
    // 널씨, 유저 정보 추출
    String text = getText(weatherId, userId);

    // llm을 통해 옷 속성간의 우선순위 도출
    String response = llmApiClient.getInfo(text);
    List<ClothingOption> clothingOptions = getOptions(response);

    // 우선순위를 통해 옷 조회 후 Dto 변환
    List<Clothes> clothes =
      clothesRepositoryQueryDSL.findAllOrderedByAttributeScores(clothingOptions, 10);
    List<RecommendationClothesDto> recommendationClothesDtos = clothes.stream()
      .map(this::toRecommendationClothesDto)
      .toList();

    return new RecommendationDto(
      weatherId,
      userId,
      recommendationClothesDtos
    );
  }

  public RecommendationDto getRecommendations(UUID weatherId, UUID userId) {
    Weather weather = getWeatherOrThrow(weatherId);
    Humidity humidity = getHumidityOrThrow(weather.getHumidityId());
    Temperature temperature = getTemperatureOrThrow(weather.getTemperatureId());
    User user = getUserOrThrow(userId);

    // 체감 온도 도출
    double adjustedFeelsLikeTemp = calculateAdjustedFeelsLikeTemp(
      temperature.getCurrent(), humidity.getCurrent(), user.getTemperatureSensitivity()
    );

    // 옷 두께 도출
    String thickness = determineThickness(adjustedFeelsLikeTemp);

    // 옷 두께를 기준으로 옷 목록 조회
    List<Clothes> selectedClothes = getRandomRecommendedClothes(thickness);

    List<RecommendationClothesDto> recommendationClothesDtos = selectedClothes.stream()
      .map(this::toRecommendationClothesDto)
      .toList();

    return new RecommendationDto(
      weatherId,
      userId,
      recommendationClothesDtos
    );
  }

  private double calculateAdjustedFeelsLikeTemp(double temp, double humidity, int sensitivity) {
    // 채검 온도 계산
    double apparentTemp = getInSummer(temp, humidity);
    // 더위 민감도에 따라 보정
    return apparentTemp + (ThreadLocalRandom.current().nextDouble(0.5, 0.8) * sensitivity);
  }

  private String determineThickness(double adjustedFeelsLikeTemp) {
    if (adjustedFeelsLikeTemp >= 20) {
      return "얇음";
    } else if (adjustedFeelsLikeTemp >= 10) {
      return "보통";
    } else {
      return "두꺼움";
    }
  }

  private List<Clothes> getRandomRecommendedClothes(String thickness) {
    List<ClothingOption> clothingOptions = List.of(
      new ClothingOption("두께", List.of(thickness))
    );

    // 조회 목록 개수 랜덤 지정
    int limit = ThreadLocalRandom.current().nextInt(20, 30);
    List<Clothes> clothes = clothesRepositoryQueryDSL.findAllOrderedByAttributeScores(
      clothingOptions, limit
    );

    // 조회 목록에서 랜덤으로 10개 추출
    List<Clothes> shuffled = new ArrayList<>(clothes);
    Collections.shuffle(shuffled);

    return shuffled.stream().limit(10).toList();
  }


  /**
   * 여름철 체감온도 (5월 ~ 9월)
   *
   * @param ta 기온
   * @param rh 상대습도
   */
  private double getInSummer(double ta, double rh) {
    double tw = getTw(ta, rh);
    return -0.2442 + (0.55399 * tw) + (0.45535 * ta) - (0.0022 * Math.pow(tw, 2.0)) + (0.00278 * tw
      * ta) + 3.0;
  }

  private double getTw(double ta, double rh) {
    return ta * Math.atan(0.151977 * Math.pow(rh + 8.313659, 0.5)) +
      Math.atan(ta + rh) - Math.atan(rh - 1.67633) +
      (0.00391838 * Math.pow(rh, 1.5) * Math.atan(0.023101 * rh)) - 4.686035;
  }

  private String getText(UUID weatherId, UUID userId) {
    String weatherInfo = getWeatherInfo(weatherId, userId);
    String clotheInfo = getClotheInfo();

    String prompt = "날씨 정보를 보고 옷 속성 정보 간의 순위를 매겨주고 옷 속성에서 선택할 수 있는 값들의 순위도 매겨줘. "
      + "다음 JSON 형식으로 응답해줘. JSON의 최상위 레벨은 배열(array)이고, 배열의 각 요소는 다음과 같은 형태의 객체여야 합니다:"
      + " {\"attribute\": \"속성이름\", \"values\": [선택 가능한 값들]}. reason은 필요없습니다.";

    return weatherInfo + clotheInfo + prompt;
  }

  private String getWeatherInfo(UUID weatherId, UUID userId) {
    Weather weather = getWeatherOrThrow(weatherId);
    Humidity humidity = getHumidityOrThrow(weather.getHumidityId());
    Precipitation precipitation = getPrecipitationOrThrow(weather.getPrecipitationId());
    Temperature temperature = getTemperatureOrThrow(weather.getTemperatureId());
    WindSpeed windSpeed = getWindSpeedOrThrow(weather.getWindSpeedId());
    User user = getUserOrThrow(userId);

    return "날씨 정보 \n"
      + "습도: " + humidity.getCurrent() + "\n"
      + "강수 타입: " + precipitation.getType() + "\n"
      + "강수량: " + precipitation.getAmount() + "\n"
      + "강수 확률: " + precipitation.getProbability() + "\n"
      + "최저 기온: " + temperature.getMin() + "\n"
      + "최고 기온: " + temperature.getMax() + "\n"
      + "현재 기온: " + temperature.getCurrent() + "\n"
      + "풍속: " + windSpeed.getSpeed() + "ms\n"
      + "바람 세기: " + windSpeed.getAsWord() + "\n"
      + "하늘 상태: " + weather.getSkyStatus() + "\n"
      + "더위 민감도: " + user.getTemperatureSensitivity() + "(1~5)\n"
      + "성별: " + user.getGender() + "\n\n";
  }

  private String getClotheInfo() {
    StringBuilder clotheInfo = new StringBuilder("옷 속성 정보 \n");

    List<ClothesAttributeDef> clothesAttributeDefs = clothesAttributeDefRepository.findAll();
    for (ClothesAttributeDef clothesAttributeDef : clothesAttributeDefs) {
      StringBuilder attribute = new StringBuilder(clothesAttributeDef.getName() + ": ");
      List<SelectableValue> selectableValues =
        selectableValueRepository.findAllByAttributeDefId(clothesAttributeDef.getId());
      for (SelectableValue electableValue : selectableValues) {
        attribute.append(electableValue.getItem()).append(", ");
      }
      clotheInfo.append(attribute).append("\n");
    }

    return String.valueOf(clotheInfo.append("\n"));
  }

  private List<ClothingOption> getOptions(String response) {
    String cleaned = response
      .replaceAll("(?i)```json\\s*", "")  // ```json 또는 ```JSON 제거
      .replaceAll("```", "") // 닫는 ``` 제거
      .trim(); // 양쪽 공백 제거

    ObjectMapper mapper = new ObjectMapper();
    List<ClothingOption> list = null;
    try {
      list = mapper.readValue(cleaned, new TypeReference<List<ClothingOption>>() {
      });
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return list;
  }

  private RecommendationClothesDto toRecommendationClothesDto(Clothes clothes) {
    List<ClothesAttribute> clothesAttributes =
      clothesAttributeRepository.findAllByClothesId(clothes.getId());

    List<UUID> selectableValueIds = clothesAttributes.stream()
      .map(ClothesAttribute::getSelectableValueId)
      .toList();

    List<RecommendationClothesAttributeDto> recommendationClothesAttributeDtos =
      selectableValueIds.stream()
        .map(this::toRecommendationClothesAttributeDto)
        .toList();

    return new RecommendationClothesDto(
      clothes.getId(),
      clothes.getName(),
      clothes.getImageUrl(),
      clothes.getType(),
      recommendationClothesAttributeDtos
    );
  }

  private RecommendationClothesAttributeDto toRecommendationClothesAttributeDto(
    UUID selectableValueId) {
    SelectableValue selectableValue = selectableValueRepository.findById(selectableValueId)
      .orElseThrow(() -> SelectableValueNotFoundException.withId(selectableValueId));

    ClothesAttributeDef clothesAttributeDef =
      clothesAttributeDefRepository.findById(selectableValue.getAttributeDefId())
        .orElseThrow(() ->
          ClothesAttributeDefNotFoundException.withId(selectableValue.getAttributeDefId()));

    List<String> selectableValues =
      selectableValueRepository.findAllByAttributeDefId(clothesAttributeDef.getId()).stream()
        .map(SelectableValue::getItem)
        .toList();

    return new RecommendationClothesAttributeDto(
      clothesAttributeDef.getId(),
      clothesAttributeDef.getName(),
      selectableValues,
      selectableValue.getItem()
    );
  }

  private Weather getWeatherOrThrow(UUID weatherId) {
    return weatherRepository.findById(weatherId)
      .orElseThrow(() ->
        WeatherNotFoundException.withId(WeatherErrorCode.WEATHER_NOF_FOUND, weatherId));
  }

  private Humidity getHumidityOrThrow(UUID humidityId) {
    return humidityRepository.findById(humidityId)
      .orElseThrow(() ->
        WeatherNotFoundException
          .withId(WeatherErrorCode.HUMIDITY_NOF_FOUND, humidityId));
  }

  private Temperature getTemperatureOrThrow(UUID temperatureId) {
    return temperatureRepository.findById(temperatureId)
      .orElseThrow(() ->
        WeatherNotFoundException
          .withId(WeatherErrorCode.TEMPERATURE_NOF_FOUND, temperatureId));
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> UserNotFoundException.withId(userId));
  }

  private Precipitation getPrecipitationOrThrow(UUID precipitationId) {
    return precipitationRepository.findById(precipitationId)
      .orElseThrow(() ->
        WeatherNotFoundException
          .withId(WeatherErrorCode.PRECIPITATION_NOF_FOUND, precipitationId));
  }

  private WindSpeed getWindSpeedOrThrow(UUID windSpeedId) {
    return windSpeedRepository.findById(windSpeedId)
      .orElseThrow(() ->
        WeatherNotFoundException
          .withId(WeatherErrorCode.WINDSPEED_NOF_FOUND, windSpeedId));
  }
}
