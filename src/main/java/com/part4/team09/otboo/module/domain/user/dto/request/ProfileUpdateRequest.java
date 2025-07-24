package com.part4.team09.otboo.module.domain.user.dto.request;

import com.part4.team09.otboo.module.domain.user.entity.User.Gender;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record ProfileUpdateRequest(
  String name,
  Gender gender,
  LocalDate birthDate,
  LocationUpdateRequest location,
  Integer temperatureSensitivity
) {

  public record LocationUpdateRequest(

    @NotNull(message = "비워둘 수 없습니다.")
    @DecimalMin(value = "-90.0", message = "유효한 범위가 아닙니다.(유효 범위: -90 ~ 90)")
    @DecimalMax(value = "90.0", message = "유효한 범위가 아닙니다.(유효 범위: -90 ~ 90)")
    Double latitude,

    @NotNull(message = "비워둘 수 없습니다.")
    @DecimalMin(value = "-180.0", message = "유효한 범위가 아닙니다.(유효 범위: -180 ~ 180)")
    @DecimalMax(value = "180.0", message = "유효한 범위가 아닙니다.(유효 범위: -180 ~ 180)")
    Double longitude,

    @NotNull(message = "비워둘 수 없습니다.")
    Integer x,
    @NotNull(message = "비워둘 수 없습니다.")
    Integer y,

    @NotNull(message = "비워둘 수 없습니다.")
    @Size(min = 2)
    List<String> locationNames
  ) {

  }
}
