package com.part4.team09.otboo.module.domain.notification.dto.request;

import com.part4.team09.otboo.module.domain.notification.entity.Notification.Level;
import java.util.UUID;

public record NotificationCreateFollowerRequest(
  UUID authorId,
  String title,
  String content,
  Level level
) {

}
