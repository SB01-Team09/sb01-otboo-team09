package com.part4.team09.otboo.module.domain.notification.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.domain.notification.dto.NotificationDto;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

  @Value("${sse.timeout}")
  private long timeout;

  private final SseEmitterRepository sseEmitterRepository;
  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public SseEmitter connect(UUID receiverId) {
    log.debug("userId: {}, emitter 수: {}", receiverId,
      sseEmitterRepository.findByReceiverId(receiverId).size());

    SseEmitter sseEmitter = new SseEmitter(timeout);

    sseEmitter.onCompletion(() -> handleCompletion(receiverId, sseEmitter));
    sseEmitter.onTimeout(() -> handleTimeout(receiverId, sseEmitter));
    sseEmitter.onError(e -> handleError(receiverId, sseEmitter, e));

    try {
      sseEmitter.send(SseEmitter.event().name("connect").data("connected"));
    } catch (IOException e) {
      sseEmitterRepository.delete(receiverId, sseEmitter);
    }

    sseEmitterRepository.save(receiverId, sseEmitter);

    return sseEmitter;
  }

  public void send(NotificationDto notificationDto) {
    try {
      String payload = objectMapper.writeValueAsString(notificationDto);
      redisTemplate.convertAndSend("notification-channel", payload);
    } catch (JsonProcessingException e) {
      log.error("NotificationDto 직렬화 실패: notificationId={}, receiverId={}",
          notificationDto.id(), notificationDto.receiverId(), e);
    }
  }

  public void sendToUsers(List<NotificationDto> notificationDtos) {
    notificationDtos.forEach(this::send);
  }

  // sse 연결 해제
  public void disconnectAllEmitters(UUID userId, String reason) {
    log.debug("Emitter 제거 작업 시작, userId: {}, reason: {}", userId, reason);
    redisTemplate.convertAndSend("disconnect-channel", userId.toString());
  }

  @Scheduled(cron = "0 */30 * * * *")
  public void cleanUp() {
    sseEmitterRepository.findAll()
        .forEach(emitter -> {
          try {
            emitter.send(SseEmitter.event()
                .name("ping")
                .data("keep-alive"));
          } catch (IOException e) {
            emitter.completeWithError(e);
          }
        });
  }

  // sse 연결종료 시
  private void handleCompletion(UUID receiverId, SseEmitter emitter) {
    log.info("SSE 연결 종료됨: receiverId={}", receiverId);
    sseEmitterRepository.delete(receiverId, emitter);
  }

  // sse 타임아웃 시 종료 처리
  private void handleTimeout(UUID receiverId, SseEmitter emitter) {
    log.warn("SSE 타임아웃 발생: receiverId={}", receiverId);
    sseEmitterRepository.delete(receiverId, emitter);
    try {
      emitter.complete();
    } catch (Exception ex) {
      log.info("emitter.complete() 중 예외 발생: {}", ex.getMessage());
    }
  }

  // 오류 발생 시
  private void handleError(UUID receiverId, SseEmitter emitter, Throwable e) {
    log.info("SSE 오류 발생: receiverId={}, error={}", receiverId, e.getMessage(), e);
    sseEmitterRepository.delete(receiverId, emitter);
    try {
      emitter.completeWithError(e);
    } catch (Exception ex) {
      log.info("emitter.completeWithError() 중 예외 발생: {}", ex.getMessage());
    }
  }
}
