package com.part4.team09.otboo.module.domain.location.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "location_names")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationName extends BaseEntity {

  @Column(nullable = false)
  private UUID locationId;

  @Column(nullable = false)
  private String item;

  public static LocationName create(UUID locationId, String item) {
    return new LocationName(locationId, item);
  }

  private LocationName(UUID locationId, String item) {
    this.locationId = locationId;
    this.item = item;
  }
}
