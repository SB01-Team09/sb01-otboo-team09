package com.part4.team09.otboo.module.domain.weather.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.domain.location.entity.Dong;
import com.part4.team09.otboo.module.domain.location.entity.Location;
import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherApiData;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse.Response.Body.Items.Item;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherReadException;
import com.part4.team09.otboo.module.domain.weather.external.WeatherApiClient;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WeatherReaderTest {

  @Mock
  private ItemStreamReader<Location> locationReader;

  @Mock
  private WeatherApiClient weatherApiClient;

  @Mock
  private DongRepository dongRepository;

  @Mock
  private WeatherRepository weatherRepository;

  @Mock
  private WeatherCache weatherCache;

  @InjectMocks
  private WeatherReader weatherReader;

  @Test
  void read_should_process_cached_data_when_cache_exists() throws Exception {
    // given
    double latitude = 126.993430555555;
    double longitude = 37.5639;
    int x = 60;
    int y = 127;

    Dong dong = Dong.create("사직동", latitude, longitude, x, y);
    UUID sidoId = UUID.randomUUID();
    UUID guId = UUID.randomUUID();
    UUID dongId = UUID.randomUUID();
    ReflectionTestUtils.setField(dong, "id", dongId);

    String locationId = "1111111111";
    Location location = Location.create(locationId, sidoId, guId, dongId);

    UUID precipitationId = UUID.randomUUID();
    UUID humidityId = UUID.randomUUID();
    UUID temperatureId = UUID.randomUUID();
    UUID windSpeedId = UUID.randomUUID();

    Weather cachedWeather = Weather.create(
      LocalDateTime.now(),
      LocalDateTime.now().plusHours(1),
      SkyStatus.CLEAR,
      locationId,
      precipitationId,
      humidityId,
      temperatureId,
      windSpeedId
    );

    when(locationReader.read())
      .thenReturn(location)
      .thenReturn(null);
    when(dongRepository.findById(dongId)).thenReturn(Optional.of(dong));
    when(weatherCache.getData(60, 127)).thenReturn(List.of(cachedWeather));
    when(weatherRepository.findByLocationIdAndForecastAt(any(), any())).thenReturn(
      Optional.empty());

    // when
    WeatherApiData result = weatherReader.read();

    // then
    verify(weatherRepository, times(1)).save(any(Weather.class));
  }

  @Test
  void read_should_process_cached_data_when_cache_exists_repository_find_exists() throws Exception {
    // given
    double latitude = 126.993430555555;
    double longitude = 37.5639;
    int x = 60;
    int y = 127;

    Dong dong = Dong.create("사직동", latitude, longitude, x, y);
    UUID sidoId = UUID.randomUUID();
    UUID guId = UUID.randomUUID();
    UUID dongId = UUID.randomUUID();
    ReflectionTestUtils.setField(dong, "id", dongId);

    String locationId = "1111111111";
    Location location = Location.create(locationId, sidoId, guId, dongId);

    UUID precipitationId = UUID.randomUUID();
    UUID humidityId = UUID.randomUUID();
    UUID temperatureId = UUID.randomUUID();
    UUID windSpeedId = UUID.randomUUID();

    Weather cachedWeather = Weather.create(
      LocalDateTime.now(),
      LocalDateTime.now().plusHours(1),
      SkyStatus.CLEAR,
      locationId,
      precipitationId,
      humidityId,
      temperatureId,
      windSpeedId
    );

    Weather savedWeather = Weather.create(
      LocalDateTime.now(),
      LocalDateTime.now().plusHours(1),
      SkyStatus.MOSTLY_CLOUDY,
      UUID.randomUUID().toString(),
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID()
    );

    when(locationReader.read())
      .thenReturn(location)
      .thenReturn(null);
    when(dongRepository.findById(dongId)).thenReturn(Optional.of(dong));
    when(weatherCache.getData(60, 127)).thenReturn(List.of(cachedWeather));
    when(weatherRepository.findByLocationIdAndForecastAt(any(), any())).thenReturn(
      Optional.of(savedWeather));

    // when
    WeatherApiData result = weatherReader.read();

    // then
    verify(weatherRepository, times(1)).save(any(Weather.class));
  }

  @Test
  void read_should_call_api_when_cache_is_empty() throws Exception {
    // given
    double latitude = 126.993430555555;
    double longitude = 37.5639;
    int x = 60;
    int y = 127;

    Dong dong = Dong.create("사직동", latitude, longitude, x, y);
    UUID sidoId = UUID.randomUUID();
    UUID guId = UUID.randomUUID();
    UUID dongId = UUID.randomUUID();
    ReflectionTestUtils.setField(dong, "id", dongId);

    String locationId = "1111111111";
    Location location = Location.create(locationId, sidoId, guId, dongId);
    Item item = new Item(
      "20250709",
      "2300",
      "TMN",
      "20250709",
      "0300",
      "20",
      x,
      y
    ); // 필요 시 필드 세팅 가능
    List<Item> items = List.of(item);

    when(locationReader.read())
      .thenReturn(location)
      .thenReturn(null);
    when(dongRepository.findById(dongId)).thenReturn(Optional.of(dong));
    when(weatherCache.getData(60, 127)).thenReturn(null);
    when(weatherApiClient.getWeatherApiResponse(60, 127)).thenReturn(items);

    // when
    WeatherApiData result = weatherReader.read();

    // then
    assertNotNull(result);
    assertEquals(locationId, result.locationId());
    assertEquals(items, result.items());
  }

  @Test
  void read_should_throw_exception_when_api_fails() throws Exception {
    // given
    double latitude = 126.993430555555;
    double longitude = 37.5639;
    int x = 60;
    int y = 127;

    Dong dong = Dong.create("사직동", latitude, longitude, x, y);
    UUID sidoId = UUID.randomUUID();
    UUID guId = UUID.randomUUID();
    UUID dongId = UUID.randomUUID();
    ReflectionTestUtils.setField(dong, "id", dongId);

    String locationId = "1111111111";
    Location location = Location.create(locationId, sidoId, guId, dongId);

    when(locationReader.read()).thenReturn(location);
    when(dongRepository.findById(dongId)).thenReturn(Optional.of(dong));
    when(weatherCache.getData(60, 127)).thenReturn(null);
    when(weatherApiClient.getWeatherApiResponse(60, 127)).thenThrow(new RuntimeException());

    // when & then
    assertThrows(WeatherReadException.class, () -> weatherReader.read());
  }
}