package com.part4.team09.otboo.module.domain.location.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dongs",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "uk_dongs_latitude_longitude",
      columnNames = {"latitude", "longitude"})
  }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dong extends BaseEntity {

  @Column(nullable = false)
  private String dongName;

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;

  @Column(nullable = false)
  private int x;

  @Column(nullable = false)
  private int y;

  public static Dong create(String dongName, double latitude, double longitude, int x, int y) {
    return new Dong(dongName, latitude, longitude, x, y);
  }

  private Dong(String dongName, double latitude, double longitude, int x, int y) {
    this.dongName = dongName;
    this.latitude = latitude;
    this.longitude = longitude;
    this.x = x;
    this.y = y;
  }
}
