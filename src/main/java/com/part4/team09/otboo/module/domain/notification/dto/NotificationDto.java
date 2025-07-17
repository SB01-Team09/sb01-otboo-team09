package com.part4.team09.otboo.module.domain.notification.dto;

import com.part4.team09.otboo.module.domain.notification.entity.Notification.Level;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
  UUID id,
  LocalDateTime createdAt,
  UUID receiverId,
  String title,
  String content,
  Level level
) {

}
