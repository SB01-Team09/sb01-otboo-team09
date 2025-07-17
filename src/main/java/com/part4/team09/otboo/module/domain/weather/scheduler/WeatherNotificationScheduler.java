//package com.part4.team09.otboo.module.domain.weather.scheduler;
//
//import com.part4.team09.otboo.module.domain.location.entity.Location;
//import com.part4.team09.otboo.module.domain.location.repository.LocationRepository;
//import com.part4.team09.otboo.module.domain.user.entity.User;
//import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.UUID;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class WeatherNotificationScheduler {
//
//  private final UserRepository userRepository;
//  private final LocationRepository locationRepository;
//
//  public void sendWeatherNotification() {
//    List<User> users = userRepository.findAll();
//
//// 2. 먼저 유저들의 locationId를 모아서 중복 없이 조회
//    Set<String> locationIds = users.stream()
//      .map(User::getLocationId)
//      .collect(Collectors.toSet());
//
//// 3. locationId → Location 맵 생성 (DB 한 번만 조회)
//    Map<String, Location> locationMap = locationRepository.findAllById(locationIds).stream()
//      .collect(Collectors.toMap(Location::getId, Function.identity()));
//
//// 4. coordinateId 기준으로 userId들을 그룹화
//    Map<UUID, List<UUID>> result = users.stream()
//      .map(user -> {
//        Location location = locationMap.get(user.getLocationId());
//        if (location == null) {
//          throw new RuntimeException("Location not found: " + user.getLocationId());
//        }
//        UUID coordinateId = location.getCoordinateId();
//        return new AbstractMap.SimpleEntry<>(coordinateId, user.getId());
//      })
//      .collect(Collectors.groupingBy(
//        Map.Entry::getKey, // coordinateId
//        Collectors.mapping(Map.Entry::getValue, Collectors.toList()) // userId 리스트
//      ));
//  }
//
//}
