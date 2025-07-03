package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedMapper;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.feed.repository.LikeRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

  private final LikeRepository likeRepository;

  private final FeedMapper feedMapper;

  private final FeedRepository feedRepository;
  private final UserRepository userRepository;

  public FeedDto create(UUID userId, UUID feedID) {
    Feed feed = getFeedOrThrow(feedID);
    User user = getUserOrThrow(userId);

    return feedMapper.toDto(feed, user, null, List.of());
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
  }

  // TODO: 피드 커스텀 예외로 변경
  private Feed getFeedOrThrow(UUID feedId) {
    return feedRepository.findById(feedId)
        .orElseThrow(() -> new IllegalArgumentException());
  }

}
