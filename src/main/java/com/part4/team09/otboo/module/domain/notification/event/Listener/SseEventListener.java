package com.part4.team09.otboo.module.domain.notification.event.Listener;

import com.part4.team09.otboo.module.domain.notification.event.NotificationCreatedEvent;
import com.part4.team09.otboo.module.domain.notification.event.NotificationCreatedMultipleEvent;
import com.part4.team09.otboo.module.domain.notification.sse.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SseEventListener {

  private final SseService sseService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNotificationCreatedEvent(NotificationCreatedEvent event) {
    sseService.send(event.notificationDto());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNotificationCreatedMultipleEvent(NotificationCreatedMultipleEvent event) {
    sseService.sendToUsers(event.notificationDtos());
  }
}
