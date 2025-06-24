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
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseUpdatableEntity {

  @Column(nullable = false)
  private UUID feedId;

  @Column(nullable = false)
  private UUID authorId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  public static Comment create(UUID feedId, UUID authorId, String content) {
    return new Comment(feedId, authorId, content);
  }

  private Comment(UUID feedId, UUID authorId, String content) {
    this.feedId = feedId;
    this.authorId = authorId;
    this.content = content;
  }
}
