package com.part4.team09.otboo.module.domain.notification.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import com.part4.team09.otboo.module.domain.feed.entity.Comment;
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
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

  @Column(nullable = false)
  private UUID receiverId;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Level level;

  public enum Level {
    INFO, WARNING, ERROR
  }

  public static Notification create(UUID receiverId, String title, String content, Level level) {
    return new Notification(receiverId, title, content, level);
  }

  private Notification(UUID receiverId, String title, String content, Level level) {
    this.receiverId = receiverId;
    this.title = title;
    this.content = content;
    this.level = level;
  }
}
