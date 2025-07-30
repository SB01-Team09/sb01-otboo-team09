package com.part4.team09.otboo.module.domain.feed.permission;

import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedPermissionEvaluator {

  private final FeedRepository feedRepository;

  public boolean isFeedAuthor(UUID userId, UUID feedId) {
    boolean result = feedRepository.findById(feedId)
        .map(feed -> feed.getAuthorId().equals(userId))
        .orElse(false);

    log.debug("피드 작성자 확인 - userId: {}, feedId: {}, 결과: {}", userId, feedId, result);

    return result;
  }
}
