package com.part4.team09.otboo.module.domain.location.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

  @Mock
  private LocationRepository locationRepository;
  @Mock
  private SidoRepository sidoRepository;
  @Mock
  private GuRepository guRepository;
  @Mock
  private DongRepository dongRepository;
  @Mock
  private LocationApiClient locationApiClient;

  @InjectMocks
  private LocationService locationService;

  private final String locationId = "1234567890";

  @Test
  void getLocation_byCoordinates_success() {
    // given
    double longitude = 127.123;
    double latitude = 37.456;

    when(locationApiClient.getLocationCode(longitude, latitude)).thenReturn(locationId);

    UUID sidoId = UUID.randomUUID();
    UUID guId = UUID.randomUUID();
    UUID dongId = UUID.randomUUID();

    Location location = Location.create(locationId, sidoId, guId, dongId);
    when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));

    Sido sido = Sido.create("서울특별시");
    Gu gu = Gu.create("성동구");
    Dong dong = Dong.create("금호동", 37.5, 127.0, 60, 127);

    when(sidoRepository.findById(sidoId)).thenReturn(Optional.of(sido));
    when(guRepository.findById(guId)).thenReturn(Optional.of(gu));
    when(dongRepository.findById(dongId)).thenReturn(Optional.of(dong));

    // when
    WeatherAPILocation result = locationService.getLocation(longitude, latitude);

    // then
    assertThat(result.latitude()).isEqualTo(dong.getLatitude());
    assertThat(result.longitude()).isEqualTo(dong.getLongitude());
    assertThat(result.x()).isEqualTo(dong.getX());
    assertThat(result.y()).isEqualTo(dong.getY());
    assertThat(result.locationNames()).containsExactly("서울특별시", "성동구", "금호동");
  }

  @Test
  void getLocation_byId_locationNotFound_shouldThrow() {
    // given
    when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

    // when & then
    assertThrows(LocationNotFoundException.class, () -> locationService.getLocation(locationId));
  }

  @Test
  void getLocation_byId_guNotFound_shouldThrow() {
    // given
    UUID sidoId = UUID.randomUUID();
    UUID guId = UUID.randomUUID();
    UUID dongId = UUID.randomUUID();

    Location location = Location.create(locationId, sidoId, guId, dongId);
    when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));

    Sido sido = Sido.create("서울특별시");
    Gu gu = Gu.create("성동구");
    when(sidoRepository.findById(sidoId)).thenReturn(Optional.of(sido));
    when(guRepository.findById(guId)).thenReturn(Optional.of(gu));

    // when & then
    assertThrows(LocationNotFoundException.class, () -> locationService.getLocation(locationId));
  }

  @Test
  void getLocation_byId_success() {
    // given
    UUID sidoId = UUID.randomUUID();
    UUID guId = UUID.randomUUID();
    UUID dongId = UUID.randomUUID();

    Location location = Location.create(locationId, sidoId, guId, dongId);
    when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));

    Sido sido = Sido.create("서울특별시");
    Gu gu = Gu.create("성동구");
    Dong dong = Dong.create("금호동", 37.5, 127.0, 60, 127);

    when(sidoRepository.findById(sidoId)).thenReturn(Optional.of(sido));
    when(guRepository.findById(guId)).thenReturn(Optional.of(gu));
    when(dongRepository.findById(dongId)).thenReturn(Optional.of(dong));

    // when
    WeatherAPILocation result = locationService.getLocation(locationId);

    // then
    assertThat(result.locationNames()).containsExactly("서울특별시", "성동구", "금호동");
    assertThat(result.latitude()).isEqualTo(37.5);
    assertThat(result.longitude()).isEqualTo(127.0);
    assertThat(result.x()).isEqualTo(60);
    assertThat(result.y()).isEqualTo(127);
  }
}