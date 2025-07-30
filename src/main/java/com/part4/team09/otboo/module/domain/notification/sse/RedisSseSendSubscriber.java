package com.part4.team09.otboo.module.domain.notification.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.domain.notification.dto.NotificationDto;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSseSendSubscriber {

  private final SseEmitterRepository sseEmitterRepository;
  private final ObjectMapper objectMapper;
  private final TaskExecutor sseTaskExecutor;

  public MessageListener messageListener() {
    return (message, pattern) -> {
      try {
        String body = new String(message.getBody());
        NotificationDto notification = objectMapper.readValue(body, NotificationDto.class);
        UUID receiverId = notification.receiverId();

        log.debug("Redis 메시지 수신 - receiverId: {}, notificationId: {}", receiverId,
            notification.id());

        List<SseEmitter> emitters = sseEmitterRepository.findByReceiverId(receiverId);

        if (emitters.isEmpty()) {
          log.debug("발견된 Emitter 없음 - receiverId: {}", receiverId);
        }

        for (SseEmitter emitter : emitters) {
          sseTaskExecutor.execute(() -> {
            try {
              log.debug("Emitter로 알림 전송 시도 - receiverId: {}, notificationId: {}", receiverId,
                  notification.id());

              emitter.send(SseEmitter.event()
                  .id(notification.id().toString())
                  .name("notifications")
                  .data(notification));

              log.debug("Emitter로 알림 전송 성공 - receiverId: {}, notificationId: {}", receiverId,
                  notification.id());
            } catch (IOException e) {
              sseEmitterRepository.delete(receiverId, emitter);
              log.info("Emitter 전송 중 예외 발생 - receiverId: {}", receiverId, e);
            }
          });
        }
      } catch (Exception e) {
        log.error("Redis 메시지 처리 중 예외 발생", e);
      }
    };
  }
}
