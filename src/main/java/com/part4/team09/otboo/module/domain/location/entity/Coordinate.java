package com.part4.team09.otboo.module.domain.location.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coordinates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coordinate extends BaseEntity {

  @Column(nullable = false)
  private int x;

  @Column(nullable = false)
  private int y;

  public static Coordinate create(int x, int y) {
    return new Coordinate(x, y);
  }

  private Coordinate(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
