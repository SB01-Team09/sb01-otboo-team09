package com.part4.team09.otboo.module.domain.location.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gu extends BaseEntity {

  @Column(nullable = false)
  private String guName;

  public static Gu create(String guName) {
    return new Gu(guName);
  }

  private Gu(String guName) {
    this.guName = guName;
  }
}
