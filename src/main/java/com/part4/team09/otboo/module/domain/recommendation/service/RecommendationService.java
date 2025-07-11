package com.part4.team09.otboo.module.domain.recommendation.service;

import com.part4.team09.otboo.module.domain.recommendation.external.LLMApiClient;
import com.part4.team09.otboo.module.domain.user.entity.User.Gender;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation.PrecipitationType;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed.AsWord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationService {

  private final LLMApiClient llmApiClient;

  public String getRecommendations() {
    String text = getText();
    String response = llmApiClient.getInfo(text);
    return response;
  }

  private String getText() {
    String weatherInfo = "날씨 정보 \n"
      + "습도: " + 20 + "\n"
      + "강수 타입: " + PrecipitationType.NONE + "\n"
      + "강수량: " + 0 + "\n"
      + "강수 확률: " + 0 + "\n"
      + "최저 기온: " + 18 + "\n"
      + "최고 기온: " + 28 + "\n"
      + "현재 기온: " + 23 + "\n"
      + "풍속: " + 2 + "ms\n"
      + "바람 세기: " + AsWord.WEAK + "\n"
      + "하늘 상태: " + SkyStatus.CLEAR + "\n"
      + "더위 민감도: " + 1 + "(1~5)\n"
      + "성별: " + Gender.FEMALE + "\n\n";

    String clotheInfo = "옷 속성 정보 \n"
      + "두께: 얇음, 보통, 두꺼움 \n"
      + "색깔: 하얀색, 빨간색, 파란색 \n"
      + "사이즈: S, M, L, XL \n"
      + "촉감: 뻣뻣함, 부드러움 \n\n";

    String prompt = "날씨 정보를 보고 옷 속성 정보 간의 순위를 매겨주고 옷 속성에서 선택할 수 있는 값들의 순위도 매겨줘. "
      + "다음 JSON 형식으로 응답해줘. JSON의 최상위 레벨은 배열(array)이고, 배열의 각 요소는 다음과 같은 형태의 객체여야 합니다:"
      + " {\"attribute\": \"속성이름\", \"values\": [선택 가능한 값들]}. reason은 필요없습니다.";

    return weatherInfo + clotheInfo + prompt;
  }

}
