package com.part4.team09.otboo.module.domain.weather.external;

import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse.Response.Body.Items.Item;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

  private final MeterRegistry meterRegistry;

  @Qualifier("weatherRestClient")
  private final RestClient restClient;

  @Value("${WEATHER_SERVICE_KEY:dev-placeholder-key}")
  private String SERVICE_KEY;

  public List<Item> getWeatherApiResponse(int x, int y) {
    String numOfRows = "944";
//    String numOfRows = "2";
    String pageNum = "1";
    String baseDate = getDate();
    String baseTime = "2300";
    String nx = String.valueOf(x);
    String ny = String.valueOf(y);
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

    ResponseEntity<WeatherApiResponse> apiResponse = restClient.get()
      .uri(url)
      .retrieve()
      .toEntity(WeatherApiResponse.class);

    meterRegistry
      .counter("method_calls", "method", "getWeatherApiResponse")
      .increment();

    return apiResponse.getBody().response().body().items().item();
  }

  private String getDate() {
    return LocalDate.now()
      .minusDays(2)
      .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
  }
}
