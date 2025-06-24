package com.part4.team09.otboo.module.domain.clothes.entity;

import com.part4.team09.otboo.module.common.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "clothes")
public class Clothes extends BaseUpdatableEntity {

  @Column(nullable = false)
  private UUID ownerId;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ClothesType type;

  @Column
  private String imageUrl;

  public enum ClothesType{
    TOP,
    BOTTOM,
    ONE_PIECE,
    OUTER,
    UNDERWEAR,
    ACCESSORY,
    SHOES,
    SOCKS,
    HAT,
    BAG,
    SCARF,
    ETC
  }

  public static Clothes create(UUID ownerId, String name, ClothesType type, String imageUrl){
    return new Clothes(ownerId, name, type, imageUrl);
  }

  private Clothes(UUID ownerId, String name, ClothesType type, String imageUrl){
    this.ownerId = ownerId;
    this.name = name;
    this.type = type;
    this.imageUrl = imageUrl;
  }
}
