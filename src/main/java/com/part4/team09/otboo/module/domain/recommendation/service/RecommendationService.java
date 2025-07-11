package com.part4.team09.otboo.module.domain.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.SelectableValueRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.custom.ClothesRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.recommendation.dto.ClothingOption;
import com.part4.team09.otboo.module.domain.recommendation.external.LLMApiClient;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
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
import java.util.List;
import java.util.UUID;
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

  public String getRecommendations() {
    UUID weatherId = UUID.fromString("47d61cc4-fcd0-40e2-ba1f-f2c697428085");
    UUID userId = UUID.fromString("f1b7659d-f990-4172-b7d0-c64ef4027ea5");
    String text = getText(weatherId, userId);
    String response = llmApiClient.getInfo(text);

    List<ClothingOption> clothingOptions = getOptions(response);
    List<Clothes> clothes =
      clothesRepositoryQueryDSL.findAllOrderedByAttributeScores(clothingOptions, 10);
    return null;
  }

  private String getText(UUID weatherId, UUID userId) {
    String weatherInfo = getWeatherInfo(weatherId, userId);

    String clotheInfo = getClotheInfo();

//    String weatherInfo = "날씨 정보 \n"
//      + "습도: " + 20 + "\n"
//      + "강수 타입: " + PrecipitationType.NONE + "\n"
//      + "강수량: " + 0 + "\n"
//      + "강수 확률: " + 0 + "\n"
//      + "최저 기온: " + 18 + "\n"
//      + "최고 기온: " + 28 + "\n"
//      + "현재 기온: " + 23 + "\n"
//      + "풍속: " + 2 + "ms\n"
//      + "바람 세기: " + AsWord.WEAK + "\n"
//      + "하늘 상태: " + SkyStatus.CLEAR + "\n"
//      + "더위 민감도: " + 1 + "(1~5)\n"
//      + "성별: " + Gender.FEMALE + "\n\n";
//
//    String clotheInfo = "옷 속성 정보 \n"
//      + "두께: 얇음, 보통, 두꺼움 \n"
//      + "색깔: 하얀색, 빨간색, 파란색 \n"
//      + "사이즈: S, M, L, XL \n"
//      + "촉감: 뻣뻣함, 부드러움 \n\n";

    String prompt = "날씨 정보를 보고 옷 속성 정보 간의 순위를 매겨주고 옷 속성에서 선택할 수 있는 값들의 순위도 매겨줘. "
      + "다음 JSON 형식으로 응답해줘. JSON의 최상위 레벨은 배열(array)이고, 배열의 각 요소는 다음과 같은 형태의 객체여야 합니다:"
      + " {\"attribute\": \"속성이름\", \"values\": [선택 가능한 값들]}. reason은 필요없습니다.";

    return weatherInfo + clotheInfo + prompt;
  }

  private String getWeatherInfo(UUID weatherId, UUID userId) {
    Weather weather = weatherRepository.findById(weatherId)
      .orElseThrow();
    Humidity humidity = humidityRepository.findById(weather.getHumidityId())
      .orElseThrow();
    Precipitation precipitation = precipitationRepository.findById(weather.getPrecipitationId())
      .orElseThrow();
    Temperature temperature = temperatureRepository.findById(weather.getTemperatureId())
      .orElseThrow();
    WindSpeed windSpeed = windSpeedRepository.findById(weather.getWindSpeedId())
      .orElseThrow();
    User user = userRepository.findById(userId)
      .orElseThrow();

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
    String text = """
      ```json
      [
        {
          "attribute": "두께",
          "values": [
            "얇음",
            "보통",
            "두꺼움"
          ]
        },
        {
          "attribute": "색깔",
          "values": [
            "하얀색",
            "파란색",
            "빨간색"
          ]
        },
        {
          "attribute": "사이즈",
          "values": [
            "M",
            "L",
            "S",
            "XL"
          ]
        },
        {
          "attribute": "촉감",
          "values": [
            "부드러움",
            "뻣뻣함"
          ]
        }
      ]
      ```
      """;

    String cleaned = response
      .replaceAll("(?i)```json\\s*", "")  // ```json 또는 ```JSON 제거
      .replaceAll("```", "")               // 닫는 ``` 제거
      .trim();                             // 양쪽 공백 제거

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

}
