package com.part4.team09.otboo.module.domain.location.service;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.location.entity.Dong;
import com.part4.team09.otboo.module.domain.location.entity.Gu;
import com.part4.team09.otboo.module.domain.location.entity.Location;
import com.part4.team09.otboo.module.domain.location.entity.Sido;
import com.part4.team09.otboo.module.domain.location.external.LocationApiClient;
import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.location.repository.GuRepository;
import com.part4.team09.otboo.module.domain.location.repository.LocationRepository;
import com.part4.team09.otboo.module.domain.location.repository.SidoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {

  private final LocationRepository locationRepository;
  private final SidoRepository sidoRepository;
  private final GuRepository guRepository;
  private final DongRepository dongRepository;
  private final LocationApiClient locationApiClient;

  // longitude 경도 <-> 기상청 y, 카카오 x 127.xxx
  // latitude 위도 <-> 기상청 x, 카카오 y 37.xxxx
  public WeatherAPILocation getLocation(double longitude, double latitude) {
    String id = locationApiClient.getLocationCode(longitude, latitude);
    return getLocation(id);
  }

  public WeatherAPILocation getLocation(String id) {
    Location location = locationRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("현재 위치 정보가 없습니다."));

    Sido sido = sidoRepository.findById(location.getSidoId())
      .orElseThrow(() -> new RuntimeException("현재 위치 시/도 정보가 없습니다."));

    Gu gu = guRepository.findById(location.getGuId())
      .orElseThrow(() -> new RuntimeException("현재 위치 구 정보가 없습니다."));

    Dong dong = dongRepository.findById(location.getDongId())
      .orElseThrow(() -> new RuntimeException("현재 위치 동 정보가 없습니다."));

    return new WeatherAPILocation(
      dong.getLatitude(),
      dong.getLongitude(),
      dong.getX(),
      dong.getY(),
      List.of(sido.getSidoName(), gu.getGuName(), dong.getDongName())
    );
  }
}
