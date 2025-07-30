package com.part4.team09.otboo.module.domain.feed.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentCacheEvictListener {

  private final CacheManager cacheManager;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void cacheEvictWhenCommentCreated(CommentCreatedEvent event) {
    log.info("댓글 등록시 캐시 무효화 이벤트 처리 시작: feedId = {}", event.feedId());

    Cache cache = cacheManager.getCache("comments");

    if (cache != null) {
      cache.evict(event.feedId());
      log.debug("캐시 무효화 완료: feedId = {}", event.feedId());
    } else {
      log.debug("comments 캐시를 찾을 수 없습니다.");
    }
    log.info("댓글 등록시 캐시 무효화 이벤트 처리 완료: feedId = {}", event.feedId());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void cacheEvictWhenCommentDeleted(CommentDeletedEvent event) {
    log.info("댓글 삭제시 캐시 무효화 이벤트 처리 시작: feedId = {}", event.feedId());

    Cache cache = cacheManager.getCache("comments");

    if (cache != null) {
      cache.evict(event.feedId());
      log.debug("캐시 무효화 완료: feedId = {}", event.feedId());
    } else {
      log.debug("comments 캐시를 찾을 수 없습니다.");
    }
    log.info("댓글 삭제시 캐시 무효화 이벤트 처리 완료: feedId = {}", event.feedId());
  }
}
