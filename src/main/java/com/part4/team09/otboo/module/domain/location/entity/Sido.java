package com.part4.team09.otboo.module.domain.location.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sidos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sido extends BaseEntity {

  @Column(nullable = false)
  private String sidoName;

  public static Sido create(String sidoName) {
    return new Sido(sidoName);
  }

  private Sido(String sidoName) {
    this.sidoName = sidoName;
  }
}
