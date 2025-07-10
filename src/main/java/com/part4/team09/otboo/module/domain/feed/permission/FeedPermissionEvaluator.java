package com.part4.team09.otboo.module.domain.feed.permission;

import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedPermissionEvaluator {

  private final FeedRepository feedRepository;

  public boolean isFeedAuthor(UUID userId, UUID feedId) {
    return feedRepository.findById(feedId)
        .map(feed -> feed.getAuthorId().equals(userId))
        .orElse(false);
  }
}
