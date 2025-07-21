package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.entity.Like;
import com.part4.team09.otboo.module.domain.feed.exception.feed.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.exception.like.LikeAlreadyExistsException;
import com.part4.team09.otboo.module.domain.feed.exception.like.LikeNotFoundException;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedDtoAssembler;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.feed.repository.LikeRepository;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

  private final LikeRepository likeRepository;

  private final FeedDtoAssembler feedDtoAssembler;

  private final FeedRepository feedRepository;
  private final UserRepository userRepository;

  @Transactional
  public FeedDto create(UUID userId, UUID feedId) {
    validateUserExists(userId);
    validateLikeNotExists(userId, feedId);
    Feed feed = getFeedOrThrow(feedId);

    Like like = Like.create(feedId, userId);
    likeRepository.save(like);
    feed.increaseLikeCount();

    return feedDtoAssembler.assemble(feedId, userId);
  }

  @Transactional
  public void delete(UUID userId, UUID feedId) {
    validateUserExists(userId);
    Feed feed = getFeedOrThrow(feedId);

    Like like = getLikeOrThrow(userId, feedId);
    likeRepository.deleteById(like.getId());
    feed.decreaseLikeCount();
  }

  public void deleteAllByFeedId(UUID feedId) {
    likeRepository.deleteAllByFeedId(feedId);
  }

  private Like getLikeOrThrow(UUID userId, UUID feedId) {
    return likeRepository.findByUserIdAndFeedId(userId, feedId)
        .orElseThrow(() -> LikeNotFoundException.withId(userId, feedId));
  }

  private Feed getFeedOrThrow(UUID feedId) {
    return feedRepository.findById(feedId)
        .orElseThrow(() -> FeedNotFoundException.withId(feedId));
  }

  public void validateLikeNotExists(UUID userId, UUID feedId) {
    if (likeRepository.existsByUserIdAndFeedId(userId, feedId)) {
      throw LikeAlreadyExistsException.withId(userId, feedId);
    }
  }

  private void validateUserExists(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw UserNotFoundException.withId(userId);
    }
  }
}
