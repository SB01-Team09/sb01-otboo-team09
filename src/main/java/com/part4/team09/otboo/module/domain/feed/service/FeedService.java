package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.feed.dto.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedUpdateRequest;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.exception.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedDtoAssembler;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherErrorCode;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherNotFoundException;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

  private final FeedRepository feedRepository;
  private final FeedDtoAssembler feedDtoAssembler;

  private final OotdService ootdService;
  private final CommentService commentService;

  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;
  private final LikeService likeService;

  // TODO: 로그인 한 사용자와 같은지 확인
  @Transactional
  public FeedDto create(UUID userId, FeedCreateRequest request) {
    validateUserExists(request.authorId());
    validateWeatherExists(request.weatherId());

    Feed feed = Feed.create(request.authorId(), request.weatherId(), request.content());
    Feed savedFeed = feedRepository.save(feed);
    ootdService.create(savedFeed.getId(), request.clothesIds());

    return feedDtoAssembler.assemble(savedFeed, userId);
  }

  // TODO: 로그인 한 사용자와 같은지 확인
  @Transactional
  public FeedDto update(UUID feedId, UUID userId, FeedUpdateRequest request) {
    Feed feed = getFeedOrThrow(feedId);
    feed.update(request.content());

    return feedDtoAssembler.assemble(feed, userId);
  }

  // TODO: 로그인 한 사용자와 같은지 확인
  @Transactional
  public void delete(UUID feedId) {
    validateFeedExists(feedId);

    // TODO: 좋아요 취소 로직 구현 후 삭제 전파 로직 추가
    ootdService.deleteAllByFeedId(feedId);
    commentService.deleteAllByFeedId(feedId);

    feedRepository.deleteById(feedId);
  }

  private Feed getFeedOrThrow(UUID feedId) {
    return feedRepository.findById(feedId)
        .orElseThrow(() -> FeedNotFoundException.withId(feedId));
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

  private void validateWeatherExists(UUID weatherId) {
    if (!weatherRepository.existsById(weatherId)) {
      throw WeatherNotFoundException.withId(WeatherErrorCode.WEATHER_NOF_FOUND, weatherId);
    }
  }
}
