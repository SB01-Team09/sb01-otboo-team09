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
@Table(name = "selectable_values")
public class SelectableValue extends BaseEntity {

  @Column(nullable = false)
  private UUID attributeDefId;

  @Column(nullable = false)
  private String item;

  public static SelectableValue create(UUID attributeDefId, String item) {
    return new SelectableValue(attributeDefId, item);
  }

  private SelectableValue(UUID attributeDefId, String item) {
    this.attributeDefId = attributeDefId;
    this.item = item;
  }
}
