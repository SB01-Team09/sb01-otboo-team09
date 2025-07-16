package com.part4.team09.otboo.module.domain.location.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dongs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dong extends BaseEntity {

  @Column(nullable = false)
  private String dongName;

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;

  public static Dong create(String dongName, double latitude, double longitude) {
    return new Dong(dongName, latitude, longitude);
  }

  private Dong(String dongName, double latitude, double longitude) {
    this.dongName = dongName;
    this.latitude = latitude;
    this.longitude = longitude;
  }
}
