package com.part4.team09.otboo.module.domain.weather.external;

import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class WeatherApiClient {

  private static final String BASE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?";

  @Qualifier("weatherRestClient")
  private final RestClient restClient;

  @Value("${WEATHER_SERVICE_KEY:dev-placeholder-key}")
  private String SERVICE_KEY;

  public WeatherApiResponse getWeatherApiResponse() {
//    String numOfRows = "944";
    String numOfRows = "2";
    String pageNum = "1";
    String baseDate = "20250624";
    String baseTime = "2300";
    String nx = "60";
    String ny = "127";
    String dataType = "JSON";

    String url = BASE_URL
      + "serviceKey=" + SERVICE_KEY
      + "&numOfRows=" + numOfRows
      + "&pageNo=" + pageNum
      + "&base_date=" + baseDate
      + "&base_time=" + baseTime
      + "&nx=" + nx
      + "&ny=" + ny
      + "&dataType=" + dataType;

    // https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?
    // serviceKey=PqP5PcapysKoLpislgY6040LRTeSYN01EMyif9%2F2MTeoEpBULrPEB6uDNG3IgfMNdDVVUGQq%2BZbeum8Be3w4YQ%3D%3D
    // &numOfRows=944
    // &pageNo=1
    // &base_date=20250624
    // &base_time=2300
    // &nx=60
    // &ny=127
    // &dataType=JSON

    ResponseEntity<WeatherApiResponse> apiResponse = restClient.get()
      .uri(url)
      .retrieve()
      .toEntity(WeatherApiResponse.class);

//    List<Item> items = apiResponse.getBody().body().items().item();
    apiResponse.getBody();
    return null;
  }
}
