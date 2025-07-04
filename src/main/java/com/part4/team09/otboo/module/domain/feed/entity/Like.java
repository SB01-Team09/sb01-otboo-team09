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
  name = "likes",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "unique_likes_feed_user",
      columnNames = {"feedId", "userId"})
  }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseEntity {

  @Column(nullable = false)
  private UUID feedId;

  @Column(nullable = false)
  private UUID userId;

  public static Like create(UUID feedId, UUID userId) {
    return new Like(feedId, userId);
  }

  private Like(UUID feedId, UUID userId) {
    this.feedId = feedId;
    this.userId = userId;
  }
}
