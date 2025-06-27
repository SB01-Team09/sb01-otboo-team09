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
@Table(name = "windSpeeds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WindSpeed extends BaseEntity {

  private double speed;

  @Enumerated(EnumType.STRING)
  private AsWord asWord;

  public enum AsWord {
    WEAK, MODERATE, STRONG;

    public static AsWord fromSpeed(double speed) {
      if (speed < 9) {
        return WEAK;
      }
      if (speed < 14) {
        return MODERATE;
      }
      return STRONG;
    }
  }

  public static WindSpeed create(double speed, AsWord asWord) {
    return new WindSpeed(speed, asWord);
  }

  private WindSpeed(double speed, AsWord asWord) {
    this.speed = speed;
    this.asWord = asWord;
  }
}
