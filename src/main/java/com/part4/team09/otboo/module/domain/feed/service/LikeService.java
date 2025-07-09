package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.entity.Like;
import com.part4.team09.otboo.module.domain.feed.exception.feed.FeedNotFoundException;
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

  // TODO: 이미 존재하는 좋아요인지 확인
  @Transactional
  public FeedDto create(UUID userId, UUID feedId) {
    validateFeedExists(feedId);
    validateUserExists(userId);

    Like like = Like.create(feedId, userId);
    likeRepository.save(like);

    return feedDtoAssembler.assemble(feedId, userId);
  }

  public void delete(UUID userId, UUID feedId) {
    validateUserExists(userId);
    validateFeedExists(feedId);

    Like like = getLikeOrThrow(userId, feedId);
    likeRepository.deleteById(like.getId());
  }

  private Like getLikeOrThrow(UUID userId, UUID feedId) {
    return likeRepository.findByUserIdAndFeedId(userId, feedId)
        .orElseThrow(() -> LikeNotFoundException.withId(userId, feedId));
  }

  public boolean isLikedByMe(UUID userId, UUID feedId) {
    return likeRepository.existsByUserIdAndFeedId(userId, feedId);
  }

  private void validateFeedExists(UUID feedId) {
    if (!feedRepository.existsById(feedId)) {
      throw FeedNotFoundException.withId(feedId);
    }
  }

  private void validateUserExists(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw UserNotFoundException.withId(userId);
    }
  }
}
