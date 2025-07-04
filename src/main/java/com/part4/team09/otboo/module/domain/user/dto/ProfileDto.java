package com.part4.team09.otboo.module.domain.user.dto;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.user.entity.User.Gender;
import java.time.LocalDate;
import java.util.UUID;

public record ProfileDto(
  UUID userId,
  String name,
  Gender gender,
  LocalDate birthDate,
  WeatherAPILocation location,
  int temperatureSensitivity,
  String profileImageUrl
) {

}
