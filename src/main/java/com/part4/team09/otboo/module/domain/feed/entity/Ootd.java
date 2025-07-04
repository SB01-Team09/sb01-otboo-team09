package com.part4.team09.otboo.module.domain.feed.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
  name = "ootds",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "unique_ootds_feed_clothes",
      columnNames = {"feedId", "clothesId"})
  }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ootd extends BaseEntity {

  @Column(nullable = false)
  private UUID feedId;

  @Column(nullable = false)
  private UUID clothesId;

  public static Ootd create(UUID feedId, UUID clothesId) {
    return new Ootd(feedId, clothesId);
  }

  private Ootd(UUID feedId, UUID clothesId) {
    this.feedId = feedId;
    this.clothesId = clothesId;
  }

}
