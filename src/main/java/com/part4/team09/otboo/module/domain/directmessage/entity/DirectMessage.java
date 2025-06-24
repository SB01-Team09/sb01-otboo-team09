package com.part4.team09.otboo.module.domain.directmessage.entity;

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
@Table(name = "direct_messages")
public class DirectMessage extends BaseEntity {

  @Column(nullable = false)
  private UUID senderId;

  @Column(nullable = false)
  private UUID receiverId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  public static DirectMessage create(UUID senderId, UUID receiverId, String content) {
    return new DirectMessage(senderId, receiverId, content);
  }

  private DirectMessage(UUID senderId, UUID receiverId, String content) {
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.content = content;
  }
}
