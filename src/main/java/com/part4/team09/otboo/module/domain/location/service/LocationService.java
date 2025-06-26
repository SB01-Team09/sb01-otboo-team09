package com.part4.team09.otboo.module.domain.location.service;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.location.entity.Dong;
import com.part4.team09.otboo.module.domain.location.entity.Gu;
import com.part4.team09.otboo.module.domain.location.entity.Location;
import com.part4.team09.otboo.module.domain.location.entity.Sido;
import com.part4.team09.otboo.module.domain.location.exception.LocationNotFoundException;
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
      .orElseThrow(() -> LocationNotFoundException.withNameAndId("location", id));

    Sido sido = sidoRepository.findById(location.getSidoId())
      .orElseThrow(() -> LocationNotFoundException.withNameAndId("sido", location.getSidoId()));

    Gu gu = guRepository.findById(location.getGuId())
      .orElseThrow(() -> LocationNotFoundException.withNameAndId("gu", location.getGuId()));

    Dong dong = dongRepository.findById(location.getDongId())
      .orElseThrow(() -> LocationNotFoundException.withNameAndId("dong", location.getDongId()));

    return new WeatherAPILocation(
      dong.getLatitude(),
      dong.getLongitude(),
      dong.getX(),
      dong.getY(),
      List.of(sido.getSidoName(), gu.getGuName(), dong.getDongName())
    );
  }
}
