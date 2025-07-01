package com.part4.team09.otboo.module.domain.weather.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "temperatures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Temperature extends BaseEntity {

  private double current;
  private Double comparedToDayBefore;
  private double min;
  private double max;

  public static Temperature create(double current, Double comparedToDayBefore, double min,
    double max) {
    return new Temperature(current, comparedToDayBefore, min, max);
  }

  private Temperature(double current, Double comparedToDayBefore, double min, double max) {
    this.current = current;
    this.comparedToDayBefore = comparedToDayBefore;
    this.min = min;
    this.max = max;
  }

  public void updateCurrent(double current) {
    this.current = current;
  }

  public void updateComparedToDayBefore(Double comparedToDayBefore) {
    this.comparedToDayBefore = comparedToDayBefore;
  }

  public void updateMin(double min) {
    this.min = min;
  }

  public void updateMax(double max) {
    this.max = max;
  }
}
