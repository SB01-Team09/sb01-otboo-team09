package com.part4.team09.otboo.module.domain.notification.sse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

  private final Map<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

  public SseEmitter save(UUID receiverId, SseEmitter sseEmitter) {
    data.putIfAbsent(receiverId, new CopyOnWriteArrayList<>());
    data.get(receiverId).add(sseEmitter);

    return sseEmitter;
  }

  public List<SseEmitter> findByReceiverId(UUID receiverId) {
    return data.getOrDefault(receiverId, List.of());
  }

  public List<SseEmitter> findAll() {
    return data.values().stream()
        .flatMap(Collection::stream)
        .toList();
  }

  public void delete(UUID receiverId, SseEmitter sseEmitter) {
    if (data.containsKey(receiverId)) {
      data.get(receiverId).remove(sseEmitter);
    }
  }
}
