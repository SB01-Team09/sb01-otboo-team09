package com.part4.team09.otboo.module.domain.weather.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "failed_locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FailedLocation extends BaseEntity {

  private String failedLocationId;
  private int retryCount = 0;

  public void addRetryCount() {
    this.retryCount++;
  }

  private FailedLocation(String failedLocationId) {
    this.failedLocationId = failedLocationId;
  }

  public static FailedLocation create(String failedLocationId) {
    return new FailedLocation(failedLocationId);
  }
}
