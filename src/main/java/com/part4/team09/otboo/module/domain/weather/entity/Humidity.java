package com.part4.team09.otboo.module.domain.weather.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "humidities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Humidity extends BaseEntity {

  private double current;
  private Double comparedToDayBefore;

  public static Humidity create(double current, Double comparedToDayBefore) {
    return new Humidity(current, comparedToDayBefore);
  }

  private Humidity(double current, Double comparedToDayBefore) {
    this.current = current;
    this.comparedToDayBefore = comparedToDayBefore;
  }

  public void updateCurrent(double current) {
    this.current = current;
  }

  public void updateComparedToDayBefore(Double comparedToDayBefore) {
    this.comparedToDayBefore = comparedToDayBefore;
  }
}
