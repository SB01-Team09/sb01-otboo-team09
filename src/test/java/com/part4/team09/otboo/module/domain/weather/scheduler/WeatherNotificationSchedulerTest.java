package com.part4.team09.otboo.module.domain.weather.scheduler;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.domain.location.entity.Location;
import com.part4.team09.otboo.module.domain.location.repository.LocationRepository;
import com.part4.team09.otboo.module.domain.notification.event.RapidTemperatureDropEvent;
import com.part4.team09.otboo.module.domain.notification.event.RapidTemperatureRiseEvent;
import com.part4.team09.otboo.module.domain.notification.event.WeatherNotificationCreateEvent;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation.PrecipitationType;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import com.part4.team09.otboo.module.domain.weather.repository.PrecipitationRepository;
import com.part4.team09.otboo.module.domain.weather.repository.TemperatureRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WeatherNotificationSchedulerTest {

  @InjectMocks
  private WeatherNotificationScheduler scheduler;

  @Mock
  private UserRepository userRepository;
  @Mock
  private LocationRepository locationRepository;
  @Mock
  private WeatherRepository weatherRepository;
  @Mock
  private PrecipitationRepository precipitationRepository;
  @Mock
  private TemperatureRepository temperatureRepository;
  @Mock
  private ApplicationEventPublisher publisher;

  private UUID coordinateId;
  private String locationId;
  private UUID precipitationId;
  private UUID temperatureId;

  @BeforeEach
  void setUp() {
    coordinateId = UUID.randomUUID();
    locationId = "location-123";
    precipitationId = UUID.randomUUID();
    temperatureId = UUID.randomUUID();
  }

  @Test
  void sendWeatherNotification_rainExpected_success() {
    // given
    User user = mock(User.class);
    when(user.getLocationId()).thenReturn(locationId);

    Location location = Location.create(locationId, UUID.randomUUID(), UUID.randomUUID(),
      UUID.randomUUID(), coordinateId);

    Weather weather = Weather.create(
      LocalDate.now().atTime(12, 0),
      LocalDateTime.now(),
      SkyStatus.CLOUDY,
      coordinateId,
      precipitationId,
      UUID.randomUUID(), // humidityId
      temperatureId,
      UUID.randomUUID()  // windSpeedId
    );

    Precipitation precipitation =
      Precipitation.create(PrecipitationType.RAIN, 10.0, 80.0);
    Temperature temperature =
      Temperature.create(25.0, 0.0, 20.0, 30.0);

    ReflectionTestUtils.setField(precipitation, "id", precipitationId);
    ReflectionTestUtils.setField(temperature, "id", temperatureId);

    when(userRepository.findAll()).thenReturn(List.of(user));
    when(locationRepository.findAllById(Set.of(locationId))).thenReturn(List.of(location));
    when(weatherRepository.findAllByCoordinateIdInAndForecastAt(anySet(), any()))
      .thenReturn(List.of(weather));
    when(precipitationRepository.findAllById(Set.of(precipitationId)))
      .thenReturn(List.of(precipitation));
    when(temperatureRepository.findAllById(Set.of(temperatureId))).thenReturn(List.of(temperature));

    // when
    scheduler.sendWeatherNotification();

    // then
    verify(publisher).publishEvent(argThat((Object e) -> {
      if (!(e instanceof WeatherNotificationCreateEvent event)) {
        return false;
      }
      return event.locationId().equals(locationId);
    }));

  }

  @Test
  void sendWeatherNotification_rapidTemperatureRise_success() {
    User user = mock(User.class);
    when(user.getLocationId()).thenReturn(locationId);

    Location location = Location.create(locationId, UUID.randomUUID(), UUID.randomUUID(),
      UUID.randomUUID(), coordinateId);

    Weather weather = Weather.create(
      LocalDate.now().atTime(12, 0),
      LocalDateTime.now(),
      SkyStatus.CLEAR,
      coordinateId,
      precipitationId,
      UUID.randomUUID(),
      temperatureId,
      UUID.randomUUID()
    );

    Precipitation precipitation =
      Precipitation.create(PrecipitationType.NONE, 0.0, 0.0);
    Temperature temperature =
      Temperature.create(30.0, 6.0, 25.0, 35.0); // 급상승
    ReflectionTestUtils.setField(precipitation, "id", precipitationId);
    ReflectionTestUtils.setField(temperature, "id", temperatureId);

    when(userRepository.findAll()).thenReturn(List.of(user));
    when(locationRepository.findAllById(Set.of(locationId))).thenReturn(List.of(location));
    when(weatherRepository.findAllByCoordinateIdInAndForecastAt(anySet(), any()))
      .thenReturn(List.of(weather));
    when(precipitationRepository.findAllById(Set.of(precipitationId)))
      .thenReturn(List.of(precipitation));
    when(temperatureRepository.findAllById(Set.of(temperatureId))).thenReturn(List.of(temperature));

    scheduler.sendWeatherNotification();

    verify(publisher).publishEvent(isA(RapidTemperatureRiseEvent.class));
  }

  @Test
  void sendWeatherNotification_rapidTemperatureDrop_success() {
    User user = mock(User.class);
    when(user.getLocationId()).thenReturn(locationId);

    Location location = Location.create(locationId, UUID.randomUUID(), UUID.randomUUID(),
      UUID.randomUUID(), coordinateId);

    Weather weather = Weather.create(
      LocalDate.now().atTime(12, 0),
      LocalDateTime.now(),
      SkyStatus.MOSTLY_CLOUDY,
      coordinateId,
      precipitationId,
      UUID.randomUUID(),
      temperatureId,
      UUID.randomUUID()
    );

    Precipitation precipitation =
      Precipitation.create(PrecipitationType.NONE, 0.0, 0.0);
    Temperature temperature =
      Temperature.create(10.0, -6.0, 5.0, 15.0); // 급하강
    ReflectionTestUtils.setField(precipitation, "id", precipitationId);
    ReflectionTestUtils.setField(temperature, "id", temperatureId);

    when(userRepository.findAll()).thenReturn(List.of(user));
    when(locationRepository.findAllById(Set.of(locationId))).thenReturn(List.of(location));
    when(weatherRepository.findAllByCoordinateIdInAndForecastAt(anySet(), any()))
      .thenReturn(List.of(weather));
    when(precipitationRepository.findAllById(Set.of(precipitationId)))
      .thenReturn(List.of(precipitation));
    when(temperatureRepository.findAllById(Set.of(temperatureId))).thenReturn(List.of(temperature));

    scheduler.sendWeatherNotification();

    verify(publisher).publishEvent(isA(RapidTemperatureDropEvent.class));
  }
}
