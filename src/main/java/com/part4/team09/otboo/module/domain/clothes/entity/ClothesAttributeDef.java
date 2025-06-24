package com.part4.team09.otboo.module.domain.clothes.entity;

import com.part4.team09.otboo.module.common.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "clothes_attribute_defs")
public class ClothesAttributeDef extends BaseUpdatableEntity {

  @Column(nullable = false)
  private String name;

  public static ClothesAttributeDef create(String name) {
    return new ClothesAttributeDef(name);
  }

  private ClothesAttributeDef(String name) {
    this.name = name;
  }
}
