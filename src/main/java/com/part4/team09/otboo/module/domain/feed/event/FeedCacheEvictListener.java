package com.part4.team09.otboo.module.domain.feed.event;

import com.part4.team09.otboo.module.domain.notification.event.FeedLikedEvent;
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
public class FeedCacheEvictListener {

  private final CacheManager cacheManager;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void cacheEvictWhenFeedCreated(FeedCreatedEvent event) {
    log.info("피드 등록시 생성일순 캐시 무효화 이벤트 처리 시작");

    Cache cache = cacheManager.getCache("feeds");

    if (cache != null) {
      cache.evict("firstPage:createdAt");
      log.debug("피드 등록시 생성일순 캐시 무효화 완료");
    } else {
      log.debug("feeds 캐시를 찾을 수 없습니다.");
    }

    log.info("피드 등록시 생성일순 피드 캐시 무효화 이벤트 처리 완료");
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void cacheEvictWhenLikeCreated(FeedLikedEvent event) {
    log.info("좋아요 등록시 좋아요순 피드 캐시 무효화 이벤트 처리 시작");

    Cache cache = cacheManager.getCache("feeds");

    if (cache != null) {
      cache.evict("firstPage:likeCount");
      log.debug("좋아요 등록시 좋아요순 피드 캐시 무효화 완료");
    } else {
      log.debug("feeds 캐시를 찾을 수 없습니다.");
    }

    log.info("좋아요 등록시 좋아요순 피드 캐시 무효화 이벤트 처리 완료");
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void cacheEvictWhenFeedDeleted(FeedDeletedEvent event) {
    log.info("피드 삭제시 캐시 무효화 이벤트 처리 시작");

    Cache cache = cacheManager.getCache("feeds");

    if (cache != null) {
      cache.evict("firstPage:createdAt");
      log.debug("피드 삭제시 생성일순 피드 목록 캐시 무효화 완료");
    } else {
      log.debug("feeds 캐시를 찾을 수 없습니다.");
    }

    if (cache != null) {
      cache.evict("firstPage:likeCount");
      log.debug("피드 삭제시 좋아요순 피드 목록 캐시 무효화 완료");
    } else {
      log.debug("feeds 캐시를 찾을 수 없습니다.");
    }
    log.info("피드 삭제시 캐시 무효화 이벤트 처리 완료");
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void cacheEvictWhenLikeDeleted(FeedLikeDeletedEvent event) {
    log.info("좋아요 삭제시 좋아요순 캐시 무효화 이벤트 처리 시작");

    Cache cache = cacheManager.getCache("feeds");

    if (cache != null) {
      cache.evict("firstPage:likeCount");
      log.debug("좋아요 삭제시 좋아요순 캐시 무효화 완료");
    } else {
      log.debug("feeds 캐시를 찾을 수 없습니다.");
    }

    log.info("좋아요 삭제시 좋아요순 피드 캐시 무효화 이벤트 처리 완료");
  }
}
