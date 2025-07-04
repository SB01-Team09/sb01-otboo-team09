package com.part4.team09.otboo.module.domain.follow.event;

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
public class FollowCacheEvictListener {

    private final CacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FollowCreatedEvent event){
        log.info("팔로우 등록시 캐시 무효화 이벤트 처리 시작: followeeId = {}, followerId = {}", event.followeeId(), event.followerId());

        Cache cache = cacheManager.getCache("followSummary");

        if (cache != null) {
            cache.evict(event.followeeId());
            cache.evict(event.followerId());
            log.debug("캐시 무효화 완료: followeeId = {}, followerId = {}", event.followeeId(), event.followerId());
        }else {
            log.debug("followSummary 캐시를 찾을 수 없습니다.");
        }

        log.info("팔로우 등록시 캐시 무효화 이벤트 처리 완료: followeeId = {}, followerId = {}", event.followeeId(), event.followerId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FollowDeletedEvent event){
        log.info("팔로우 삭제시 캐시 무효화 이벤트 처리 시작: followeeId = {}, followerId = {}", event.followeeId(), event.followerId());

        Cache cache = cacheManager.getCache("followSummary");

        if (cache != null) {
            cache.evict(event.followeeId());
            cache.evict(event.followerId());
            log.debug("캐시 무효화 완료: followeeId = {}, followerId = {}", event.followeeId(), event.followerId());
        }else {
            log.debug("followSummary 캐시를 찾을 수 없습니다.");
        }

        log.info("팔로우 삭제시 캐시 무효화 이벤트 처리 완료: followeeId = {}, followerId = {}", event.followeeId(), event.followerId());
    }
}
