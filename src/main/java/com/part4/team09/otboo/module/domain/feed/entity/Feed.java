package com.part4.team09.otboo.module.domain.feed.entity;

import com.part4.team09.otboo.module.common.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feeds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed extends BaseUpdatableEntity {

  @Column(nullable = false)
  private UUID authorId;

  @Column(nullable = false)
  private UUID weatherId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false)
  private int likeCount;

  @Column(nullable = false)
  private int commentCount;

  public static Feed create(UUID authorId, UUID weatherId, String content) {
    return new Feed(authorId, weatherId, content, 0, 0);
  }

  private Feed(UUID authorId, UUID weatherId, String content, int likeCount, int commentCount) {
    this.authorId = authorId;
    this.weatherId = weatherId;
    this.content = content;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
  }
}
