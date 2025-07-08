package com.part4.team09.otboo.module.domain.weather.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.config.WeatherApiClientTestConfig;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse.Response.Body.Items.Item;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(WeatherApiClient.class)
@Import(WeatherApiClientTestConfig.class)
class WeatherApiClientTest {

  private static final String BASE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?";

  @Autowired
  private MeterRegistry meterRegistry;

  @Autowired
  private WeatherApiClient weatherApiClient;

  @Value("${WEATHER_SERVICE_KEY:dev-placeholder-key}")
  private String SERVICE_KEY;

  @Autowired
  private MockRestServiceServer mockServer;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void getWeatherApiResponse_success() throws JsonProcessingException {
    // given
    int x = 60;
    int y = 127;

    String numOfRows = "944";
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

    String response = objectMapper.writeValueAsString(getWeatherApiResponse());

    mockServer.expect(requestTo(url))
      .andExpect(method(org.springframework.http.HttpMethod.GET))
      .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

    // when
    List<Item> items = weatherApiClient.getWeatherApiResponse(x, y);

    // then
    assertEquals(2, items.size());
  }

  private WeatherApiResponse getWeatherApiResponse() {
    return new WeatherApiResponse(
      new WeatherApiResponse.Response(
        new WeatherApiResponse.Response.Header(
          "00",
          "NORMAL_SERVICE"
        ),
        new WeatherApiResponse.Response.Body(
          "JSON",
          new WeatherApiResponse.Response.Body.Items(
            List.of(
              new WeatherApiResponse.Response.Body.Items.Item(
                "20250629",
                "2300",
                "TMP",
                "20250630",
                "0000",
                "25",
                60,
                127
              ),
              new WeatherApiResponse.Response.Body.Items.Item(
                "20250629",
                "2300",
                "UUU",
                "20250630",
                "0000",
                "0.5",
                60,
                127
              )
            )
          ),
          1,   // pageNo
          944,  // numOfRows
          980  // totalCount
        )
      )
    );
  }

  private String getDate() {
    return LocalDate.now()
      .minusDays(2)
      .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
  }

}