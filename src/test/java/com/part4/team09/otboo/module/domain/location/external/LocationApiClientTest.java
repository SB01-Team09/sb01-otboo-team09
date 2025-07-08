package com.part4.team09.otboo.module.domain.location.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.config.LocationApiClientTestConfig;
import com.part4.team09.otboo.module.domain.location.dto.response.LocationApiResponse;
import com.part4.team09.otboo.module.domain.location.dto.response.LocationApiResponse.Meta;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(LocationApiClient.class)
@Import(LocationApiClientTestConfig.class)
class LocationApiClientTest {

  @Autowired
  private LocationApiClient locationApiClient;

  @Autowired
  private MockRestServiceServer mockServer;

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${KAKAO_REST_API_KEY:dev-placeholder-key}")
  private String apiKey;

  @Test
  void getLocationCode_success() throws Exception {
    // given
    double longitude = 126.993430555555;
    double latitude = 37.5639;
    String url =
      "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x=" + longitude + "&y=" + latitude;

    // 응답 데이터 준비 (LocationApiResponse와 documents 구조에 맞게)
    LocationApiResponse.Document doc1 = new LocationApiResponse.Document(
      "B",
      "서울특별시 중구 초동",
      "서울특별시",
      "중구",
      "초동",
      "",
      "1114015900",
      126.99218104827933,
      37.56440579832334
    );
    LocationApiResponse.Document doc2 = new LocationApiResponse.Document(
      "H",
      "서울특별시 중구 을지로동",
      "서울특별시",
      "중구",
      "을지로동",
      "",
      "1114060500",
      126.99135225472774,
      37.566701478579624
    );
    LocationApiResponse apiResponse = new LocationApiResponse(new Meta(2), List.of(doc1, doc2));
    String responseBody = objectMapper.writeValueAsString(apiResponse);

    mockServer.expect(requestTo(url))
      .andExpect(method(org.springframework.http.HttpMethod.GET))
      .andExpect(header("Authorization", "KakaoAK " + apiKey))
      .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

    // when
    String code = locationApiClient.getLocationCode(longitude, latitude);

    // then
    assertEquals("1114060500", code); // get(1).code()를 반환하므로 두 번째 코드
  }

  @Test
  void getLocationCode_no_data() throws Exception {
    // given
    double longitude = 127.1086228;
    double latitude = 37.4012191;
    String url =
      "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x=" + longitude + "&y=" + latitude;

    // documents가 비어있는 응답
    LocationApiResponse apiResponse = new LocationApiResponse(new Meta(2), List.of());
    String responseBody = objectMapper.writeValueAsString(apiResponse);

    mockServer.expect(requestTo(url))
      .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

    // when & then
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      locationApiClient.getLocationCode(longitude, latitude);
    });
    assertTrue(exception.getMessage().contains("좌표에 해당하는 주소를 찾을 수 없습니다."));
  }
}
