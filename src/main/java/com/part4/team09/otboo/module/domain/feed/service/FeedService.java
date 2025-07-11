package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDtoCursorResponse;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.entity.Ootd;
import com.part4.team09.otboo.module.domain.feed.exception.feed.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedDtoAssembler;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedMapper;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherSummaryDto;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherErrorCode;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherNotFoundException;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

  private final FeedRepository feedRepository;
  private final FeedRepositoryQueryDSL feedRepositoryQueryDSL;
  private final FeedDtoAssembler feedDtoAssembler;
  private final FeedMapper feedMapper;

  private final OotdService ootdService;
  private final CommentService commentService;

  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;
  private final LikeService likeService;

  // 피드 등록
  @Transactional
  public FeedDto create(UUID userId, FeedCreateRequest request) {
    validateUserExists(request.authorId());
    validateWeatherExists(request.weatherId());

    Feed feed = Feed.create(request.authorId(), request.weatherId(), request.content());
    Feed savedFeed = feedRepository.save(feed);
    ootdService.create(savedFeed.getId(), request.clothesIds());

    return feedDtoAssembler.assemble(savedFeed, userId);
  }

  @PreAuthorize("@feedPermissionEvaluator.isFeedAuthor(principal.id, #feedId)")
  @Transactional
  public FeedDto update(UUID feedId, UUID userId, FeedUpdateRequest request) {
    Feed feed = getFeedOrThrow(feedId);
    feed.update(request.content());

    return feedDtoAssembler.assemble(feed, userId);
  }

  @PreAuthorize("hasRole('ADMIN') or @feedPermissionEvaluator.isFeedAuthor(principal.id, #feedId)")
  @Transactional
  public void delete(UUID feedId) {
    validateFeedExists(feedId);

    ootdService.deleteAllByFeedId(feedId);
    commentService.deleteAllByFeedId(feedId);
    likeService.deleteAllByFeedId(feedId);

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

  // cursor 인코딩 로직 (LocalDateTime -> String)
  private String encodeCursor(LocalDateTime cursor) {
    return cursor == null ? null : cursor.toString();
  }

  // cursor 디코딩 로직 (String -> LocalDateTime)
  private LocalDateTime decodeCursor(String cursor){
    return cursor == null || cursor.isEmpty() ? null : LocalDateTime.parse(cursor);
  }
}
