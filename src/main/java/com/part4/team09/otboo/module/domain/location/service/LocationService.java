package com.part4.team09.otboo.module.domain.location.service;

import com.part4.team09.otboo.module.domain.location.dto.response.LocationApiResponse;
import com.part4.team09.otboo.module.domain.location.entity.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class LocationService {

  private static final String BASE_URL = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?";

  private final RestClient restClient;

  // longitude 경도 <-> 기상청 y, 카카오 x 127.xxx
  // latitude 위도 <-> 기상청 x, 카카오 y 37.xxxx
  public Location createLocation(double longitude, double latitude) {

    String requestX = "x=" + String.valueOf(longitude);
    String requestY = "y=" + String.valueOf(latitude);
    String requestUrl = BASE_URL + requestX + "&" + requestY;

    ResponseEntity<LocationApiResponse> response =
      restClient.get()
        .uri(requestUrl)
        .header("Authorization", "KakaoAK 레스트 키")
        .retrieve()
        .toEntity(LocationApiResponse.class);

    LocationApiResponse apiResponse = response.getBody();

    return null;
  }

}
