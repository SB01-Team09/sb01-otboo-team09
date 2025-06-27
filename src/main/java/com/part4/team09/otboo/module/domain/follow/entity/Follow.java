package com.part4.team09.otboo.module.domain.follow.entity;

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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "follows",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UniqueNumberAndStatus",
            columnNames = {"followeeId", "followerId"})
    }
)
public class Follow extends BaseEntity {

  @Column(nullable = false)
  private UUID followeeId;

  @Column(nullable = false)
  private UUID followerId;

  public static Follow create(UUID followeeId, UUID followerId) {
    return new Follow(followeeId, followerId);
  }

  private Follow(UUID followeeId, UUID followerId) {
    this.followeeId = followeeId;
    this.followerId = followerId;
  }
}
