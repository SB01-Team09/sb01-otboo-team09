package com.part4.team09.otboo.module.domain.notification.sse;

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
public class RedisSseDisconnectSubscriber {

  private final SseEmitterRepository sseEmitterRepository;
  private final TaskExecutor sseTaskExecutor;

  public MessageListener messageListener() {
    return (message, pattern) -> {
      try {
        UUID userId = UUID.fromString(new String(message.getBody()));

        List<SseEmitter> emitters = sseEmitterRepository.findByReceiverId(userId);
        log.debug("기존 Emitter 연결 수: {}, userId: {}", emitters.size(), userId);

        for (SseEmitter emitter : emitters) {
          sseTaskExecutor.execute(() -> {
            try {
              emitter.complete();
              log.debug("Emitter 정상 종료 - userId: {}", userId);
            } catch (Exception e) {
              log.warn("Emitter 종료 중 예외 - userId: {}, error: {}", userId, e.getMessage());
            } finally {
              sseEmitterRepository.delete(userId, emitter);
            }
          });
        }
      } catch (Exception e) {
        log.error("SSE 연결 해제 메시지 처리 중 오류", e);
      }
    };
  }
}
