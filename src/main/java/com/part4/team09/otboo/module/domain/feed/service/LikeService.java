package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.entity.Like;
import com.part4.team09.otboo.module.domain.feed.event.FeedLikeDeletedEvent;
import com.part4.team09.otboo.module.domain.feed.exception.feed.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.exception.like.LikeAlreadyExistsException;
import com.part4.team09.otboo.module.domain.feed.exception.like.LikeNotFoundException;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedDtoAssembler;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.feed.repository.LikeRepository;
import com.part4.team09.otboo.module.domain.notification.event.FeedLikedEvent;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

  private final LikeRepository likeRepository;

  private final FeedDtoAssembler feedDtoAssembler;

  private final FeedRepository feedRepository;
  private final UserRepository userRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public FeedDto create(UUID userId, UUID feedId) {
    log.info("좋아요 생성 요청 - 사용자ID: {}, 피드ID: {}", userId, feedId);

    validateLikeNotExists(userId, feedId);
    User user = getUserOrThrow(userId);
    Feed feed = getFeedOrThrow(feedId);

    Like like = Like.create(feedId, userId);
    likeRepository.save(like);
    feed.increaseLikeCount();

    eventPublisher.publishEvent(
        new FeedLikedEvent(
            feed.getAuthorId(),
            user.getName(),
            feed.getContent()
        )
    );

    log.info("좋아요 생성 완료 - 사용자ID: {}, 피드ID: {}", userId, feedId);
    return feedDtoAssembler.assemble(feedId, userId);
  }

  @Transactional
  public void delete(UUID userId, UUID feedId) {
    log.info("좋아요 삭제 요청 - 사용자ID: {}, 피드ID: {}", userId, feedId);

    getUserOrThrow(userId);
    Feed feed = getFeedOrThrow(feedId);

    Like like = getLikeOrThrow(userId, feedId);
    likeRepository.deleteById(like.getId());
    feed.decreaseLikeCount();

    eventPublisher.publishEvent(new FeedLikeDeletedEvent());

    log.info("좋아요 삭제 완료 - 사용자ID: {}, 피드ID: {}", userId, feedId);
  }

  public void deleteAllByFeedId(UUID feedId) {
    log.warn("피드 관련 좋아요 전체 삭제 요청 - 피드ID: {}", feedId);

    likeRepository.deleteAllByFeedId(feedId);
    eventPublisher.publishEvent(new FeedLikeDeletedEvent());

    log.info("피드 관련 좋아요 전체 삭제 완료 - 피드ID: {}", feedId);
  }

  private Like getLikeOrThrow(UUID userId, UUID feedId) {
    return likeRepository.findByUserIdAndFeedId(userId, feedId)
        .orElseThrow(() -> {
          log.error("좋아요 조회 실패 - 사용자ID: {}, 피드ID: {}", userId, feedId);
          return LikeNotFoundException.withId(userId, feedId);
        });
  }

  private Feed getFeedOrThrow(UUID feedId) {
    return feedRepository.findById(feedId)
        .orElseThrow(() -> {
          log.error("피드 조회 실패 - 피드ID: {}", feedId);
          return FeedNotFoundException.withId(feedId);
        });
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> {
          log.error("사용자 조회 실패 - 사용자ID: {}", userId);
          return UserNotFoundException.withId(userId);
        });
  }

  private void validateLikeNotExists(UUID userId, UUID feedId) {
    if (likeRepository.existsByUserIdAndFeedId(userId, feedId)) {
      log.error("좋아요 중복 확인 실패 - 이미 존재하는 좋아요, 사용자ID: {}, 피드ID: {}", userId, feedId);
      throw LikeAlreadyExistsException.withId(userId, feedId);
    }
  }
}
