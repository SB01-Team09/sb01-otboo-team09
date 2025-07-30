package com.part4.team09.otboo.module.domain.notification.permission;

import com.part4.team09.otboo.module.domain.notification.repository.NotificationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPermissionEvaluator {

  private final NotificationRepository notificationRepository;

  public boolean isNotificationReceiver(UUID userId, UUID notificationId) {
    boolean result = notificationRepository.findById(notificationId)
        .map(notification -> notification.getReceiverId().equals(userId))
        .orElse(false);

    log.debug("Notification 수신자 확인 - userId: {}, notificationId: {}, 결과: {}", userId,
        notificationId, result);

    return result;
  }

}
