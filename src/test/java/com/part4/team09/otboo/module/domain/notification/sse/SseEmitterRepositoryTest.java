package com.part4.team09.otboo.module.domain.notification.sse;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterRepositoryTest {

  private SseEmitterRepository repository;

  @BeforeEach
  void setUp() {
    repository = new SseEmitterRepository();
  }

  @Test
  @DisplayName("findByReceiverId 조회 - 결과 반환")
  void findByReceiverId_success() {
    // given
    UUID userId = UUID.randomUUID();
    SseEmitter emitter = new SseEmitter();

    // when
    repository.save(userId, emitter);
    List<SseEmitter> result = repository.findByReceiverId(userId);

    // then
    then(result).containsExactly(emitter);
  }

  @Test
  @DisplayName("findByReceiverId 조회 - 빈 리스트")
  void findByReceiverId_returnEmptyList() {
    // given
    UUID unknownId = UUID.randomUUID();

    // when
    List<SseEmitter> result = repository.findByReceiverId(unknownId);

    // then
    then(result).isEmpty();
  }

  @Test
  @DisplayName("findAll 조회")
  void findAll_success() {
    // given
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();
    SseEmitter emitter1 = new SseEmitter();
    SseEmitter emitter2 = new SseEmitter();

    repository.save(userId1, emitter1);
    repository.save(userId2, emitter2);

    // when
    List<SseEmitter> all = repository.findAll();

    // then
    then(all).containsExactlyInAnyOrder(emitter1, emitter2);
  }

  @Test
  @DisplayName("delete - 성공")
  void delete_success() {
    // given
    UUID userId = UUID.randomUUID();
    SseEmitter emitter1 = new SseEmitter();
    SseEmitter emitter2 = new SseEmitter();

    repository.save(userId, emitter1);
    repository.save(userId, emitter2);

    // when
    repository.delete(userId, emitter1);

    // then
    List<SseEmitter> remaining = repository.findByReceiverId(userId);
    then(remaining).containsExactly(emitter2);
  }

  @Test
  @DisplayName("delete - 없는 emitter (아무 일도 안 일어남)")
  void delete_thenNoException() {
    // given
    UUID unknownId = UUID.randomUUID();
    SseEmitter emitter = new SseEmitter();

    // when
    repository.delete(unknownId, emitter);

    // then
    then(repository.findAll()).isEmpty();
  }
}
