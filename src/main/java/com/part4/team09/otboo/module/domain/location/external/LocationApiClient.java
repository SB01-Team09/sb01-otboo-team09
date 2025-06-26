package com.part4.team09.otboo.module.domain.location.external;

import com.part4.team09.otboo.module.domain.location.dto.response.LocationApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class LocationApiClient {

  private static final String BASE_URL = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?";

  private final RestClient restClient;

  @Value("${KAKAO_REST_API_KEY:dev-placeholder-key}")
  private String API_KEY;

  public String getLocationCode(double longitude, double latitude) {
    String url = BASE_URL + "x=" + longitude + "&y=" + latitude;

    ResponseEntity<LocationApiResponse> response = restClient.get()
      .uri(url)
      .header("Authorization", "KakaoAK " + API_KEY)
      .retrieve()
      .toEntity(LocationApiResponse.class);

    LocationApiResponse apiResponse = response.getBody();

    if (apiResponse == null || apiResponse.documents().isEmpty()) {
      throw new RuntimeException("좌표에 해당하는 주소를 찾을 수 없습니다."); // 추후 예외 수정
    }

    return apiResponse.documents().get(1).code();
  }
}

