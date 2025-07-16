package com.part4.team09.otboo.module.domain.notification.permission;

import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.notification.repository.NotificationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationPermissionEvaluator {

  private final NotificationRepository notificationRepository;

  public boolean isNotificationReceiver(UUID userId, UUID notificationId) {
    return notificationRepository.findById(notificationId)
        .map(notification -> notification.getReceiverId().equals(userId))
        .orElse(false);
  }
}
