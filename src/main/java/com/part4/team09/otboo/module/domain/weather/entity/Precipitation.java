package com.part4.team09.otboo.module.domain.weather.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "precipitations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Precipitation extends BaseEntity {

  @Enumerated(EnumType.STRING)
  private PrecipitationType type;
  private double amount;
  private double probability;

  public enum PrecipitationType {
    NONE, RAIN, RAIN_SNOW, SNOW, SHOWER
  }

  public static Precipitation create(PrecipitationType type, double amount, double probability) {
    return new Precipitation(type, amount, probability);
  }

  private Precipitation(PrecipitationType type, double amount, double probability) {
    this.type = type;
    this.amount = amount;
    this.probability = probability;
  }
}
