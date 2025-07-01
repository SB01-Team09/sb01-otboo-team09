package com.part4.team09.otboo.module.domain.weather.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weathers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Weather extends BaseEntity {

  @Column(nullable = false)
  private LocalDateTime forecastedAt;

  @Column(nullable = false)
  private LocalDateTime forecastAt;

  @Enumerated(EnumType.STRING)
  private SkyStatus skyStatus;

  @Column(nullable = false)
  private String locationId;

  @Column(nullable = false)
  private UUID precipitationId;

  @Column(nullable = false)
  private UUID humidityId;

  @Column(nullable = false)
  private UUID temperatureId;

  @Column(nullable = false)
  private UUID windSpeedId;

  public enum SkyStatus {
    CLEAR, MOSTLY_CLOUDY, CLOUDY;

    public static SkyStatus of(int value) {
      if (value == 1) {
        return CLEAR;
      }
      if (value == 3) {
        return MOSTLY_CLOUDY;
      }
      return CLOUDY;
    }
  }

  public static Weather create(LocalDateTime forecastAt, LocalDateTime forecastedAt, SkyStatus skyStatus,
    String locationId, UUID precipitationId, UUID humidityId, UUID temperatureId,
    UUID windSpeedId) {
    return new Weather(forecastAt, forecastedAt, skyStatus, locationId, precipitationId, humidityId,
        temperatureId, windSpeedId);
  }

  private Weather (LocalDateTime forecastAt, LocalDateTime forecastedAt, SkyStatus skyStatus,
    String locationId, UUID precipitationId, UUID humidityId, UUID temperatureId,
    UUID windSpeedId) {
    this.forecastAt = forecastAt;
    this.forecastedAt = forecastedAt;
    this.skyStatus = skyStatus;
    this.locationId = locationId;
    this.precipitationId = precipitationId;
    this.humidityId = humidityId;
    this.temperatureId = temperatureId;
    this.windSpeedId = windSpeedId;
  }
}
