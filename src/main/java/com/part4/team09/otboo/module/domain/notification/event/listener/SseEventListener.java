package com.part4.team09.otboo.module.domain.notification.event.listener;

import com.part4.team09.otboo.module.domain.notification.event.NotificationCreatedEvent;
import com.part4.team09.otboo.module.domain.notification.event.NotificationCreatedMultipleEvent;
import com.part4.team09.otboo.module.domain.notification.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseEventListener {

  private final SseService sseService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNotificationCreatedEvent(NotificationCreatedEvent event) {
    try {
      sseService.send(event.notificationDto());
    } catch (Exception e) {
      log.error("SSE 전송 실패: NotificationCreatedEvent for notificationId={}",
          event.notificationDto().id(), e);
    }
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNotificationCreatedMultipleEvent(NotificationCreatedMultipleEvent event) {
    try {
      sseService.sendToUsers(event.notificationDtos());
    } catch (Exception e) {
      log.error("SSE 전송 실패: NotificationCreatedMultipleEvent for count={}",
          event.notificationDtos().size(), e);
    }
  }
}
