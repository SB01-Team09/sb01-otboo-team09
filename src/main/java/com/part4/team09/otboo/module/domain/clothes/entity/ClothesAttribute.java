package com.part4.team09.otboo.module.domain.clothes.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "clothes_attributes")
public class ClothesAttribute extends BaseEntity {

  @Column(nullable = false)
  private UUID clothesId;

  @Column(nullable = false)
  private UUID selectableValueId;

  public static ClothesAttribute create(UUID clothesId, UUID selectableValueId){
    return new ClothesAttribute(clothesId, selectableValueId);
  }

  private ClothesAttribute(UUID clothesId, UUID selectableValueId) {
    this.clothesId = clothesId;
    this.selectableValueId = selectableValueId;
  }
}
