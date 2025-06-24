package com.part4.team09.otboo.module.domain.location.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location extends BaseEntity {

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;

  @Column(nullable = false)
  private int x;

  @Column(nullable = false)
  private int y;

  public static Location create(double latitude, double longitude, int x, int y) {
    return new Location(latitude, longitude, x, y);
  }

  private Location(double latitude, double longitude, int x, int y) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.x = x;
    this.y = y;
  }
}
