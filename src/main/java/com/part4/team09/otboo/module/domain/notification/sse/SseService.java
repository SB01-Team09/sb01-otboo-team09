package com.part4.team09.otboo.module.domain.notification.sse;

import com.part4.team09.otboo.module.domain.notification.dto.NotificationDto;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SseService {

  @Value("${sse.timeout}")
  private long timeout;

  private final SseEmitterRepository sseEmitterRepository;

  public SseEmitter connect(UUID receiverId) {
    SseEmitter sseEmitter = new SseEmitter(timeout);

    sseEmitter.onCompletion(() -> sseEmitterRepository.delete(receiverId, sseEmitter));
    sseEmitter.onTimeout(() -> sseEmitterRepository.delete(receiverId, sseEmitter));
    sseEmitter.onError(e -> sseEmitterRepository.delete(receiverId, sseEmitter));

    try {
      sseEmitter.send(SseEmitter.event().name("connect").data("connected"));
    } catch (IOException e) {
      sseEmitterRepository.delete(receiverId, sseEmitter);
    }

    sseEmitterRepository.save(receiverId, sseEmitter);

    return sseEmitter;
  }

  public void send(NotificationDto notificationDto) {
    UUID receiverId = notificationDto.receiverId();
    List<SseEmitter> emitters = sseEmitterRepository.findByReceiverId(receiverId);

    emitters.forEach(emitter -> {
      try {
        emitter.send(SseEmitter.event()
            .id(notificationDto.id().toString())
            .name("notifications")
            .data(notificationDto));
      } catch (IOException e) {
        sseEmitterRepository.delete(receiverId, emitter);
      }
    });
  }

  public void sendToUsers(List<NotificationDto> notificationDtos) {
    notificationDtos.forEach(this::send);
  }
}
