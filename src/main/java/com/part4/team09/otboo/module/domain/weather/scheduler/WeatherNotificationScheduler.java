package com.part4.team09.otboo.module.domain.weather.scheduler;

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
import com.part4.team09.otboo.module.domain.weather.repository.PrecipitationRepository;
import com.part4.team09.otboo.module.domain.weather.repository.TemperatureRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WeatherNotificationScheduler implements CommandLineRunner {

  private final UserRepository userRepository;
  private final LocationRepository locationRepository;
  private final WeatherRepository weatherRepository;
  private final PrecipitationRepository precipitationRepository;
  private final TemperatureRepository temperatureRepository;
  private final ApplicationEventPublisher publisher;

  @Override
  public void run(String... args) throws Exception {
//    sendWeatherNotification();
  }

  @SchedulerLock(name = "WeatherNotificationScheduler", lockAtLeastFor = "PT20M")
  @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul") // 초 분 시 일 월 요일 매일 새벽 5시
  @Transactional(readOnly = true)
  public void sendWeatherNotification() {
    // 1. 유저 리스트 조회
    List<User> users = userRepository.findAll();

    // 2. 유저의 locationId 수집
    Set<String> locationIds = users.stream()
      .map(User::getLocationId)
      .collect(Collectors.toSet());

    // 3. Location 조회 (locationId → coordinateId)
    List<Location> locations = locationRepository.findAllById(locationIds);

    // 4. coordinateId → locationId 리스트 맵 만들기
    Map<UUID, List<String>> coordinateToLocationIds = locations.stream()
      .collect(Collectors.groupingBy(
        Location::getCoordinateId,
        Collectors.mapping(Location::getId, Collectors.toList())
      ));

    // 5. coordinateId 리스트 수집
    Set<UUID> coordinateIds = coordinateToLocationIds.keySet();

    // 6. coordinateId 기반으로 Weather 조회 (1:1 관계)
    LocalDateTime forecastAt = LocalDate.now().atTime(12, 0);
    List<Weather> weathers =
      weatherRepository.findAllByCoordinateIdInAndForecastAt(coordinateIds, forecastAt);

    // 7. Weather → Map<coordinateId, Weather>
    Map<UUID, Weather> weatherMap = weathers.stream()
      .collect(Collectors.toMap(Weather::getCoordinateId, Function.identity()));

    // 8. precipitationId, temperatureId 수집
    Set<UUID> precipitationIds = weathers.stream()
      .map(Weather::getPrecipitationId)
      .collect(Collectors.toSet());

    Set<UUID> temperatureIds = weathers.stream()
      .map(Weather::getTemperatureId)
      .collect(Collectors.toSet());

    // 9. 엔티티 batch 조회
    List<Precipitation> precipitations = precipitationRepository.findAllById(precipitationIds);
    List<Temperature> temperatures = temperatureRepository.findAllById(temperatureIds);

    // 10. Map 변환
    Map<UUID, Precipitation> precipitationMap = precipitations.stream()
      .collect(Collectors.toMap(Precipitation::getId, Function.identity()));

    Map<UUID, Temperature> temperatureMap = temperatures.stream()
      .collect(Collectors.toMap(Temperature::getId, Function.identity()));

    for (UUID coordinateId : coordinateToLocationIds.keySet()) {
      Weather weather = weatherMap.get(coordinateId);
      if (weather == null) {
        continue;
      }

      Precipitation precipitation = precipitationMap.get(weather.getPrecipitationId());
      Temperature temperature = temperatureMap.get(weather.getTemperatureId());

      List<String> locationIdsForCoord = coordinateToLocationIds.get(coordinateId);

      for (String locationId : locationIdsForCoord) {
        if (precipitation.getType() != PrecipitationType.NONE) {
          String title = "오늘은 " + precipitation.getType().getKorean() + "가(이) 올 예정입니다.";
          String content = "외출 시 우산을 챙기세요.";
          publisher.publishEvent(new WeatherNotificationCreateEvent(
            locationId, title, content
          ));
        }
        if (temperature.getComparedToDayBefore() > 5) {
          publisher.publishEvent(new RapidTemperatureRiseEvent(locationId));
        }
        if (temperature.getComparedToDayBefore() < -5) {
          publisher.publishEvent(new RapidTemperatureDropEvent(locationId));
        }
      }
    }
  }
}
