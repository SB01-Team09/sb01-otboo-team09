package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedListRequest;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.event.FeedCreatedEvent;
import com.part4.team09.otboo.module.domain.feed.event.FeedDeletedEvent;
import com.part4.team09.otboo.module.domain.feed.exception.feed.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedDtoAssembler;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherErrorCode;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherNotFoundException;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

  private final FeedRepository feedRepository;
  private final FeedDtoAssembler feedDtoAssembler;
  private final FeedRepositoryQueryDSL feedRepositoryQueryDSL;

  private final OotdService ootdService;

  private final CommentService commentService;

  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;
  private final LikeService likeService;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public FeedDto create(UUID userId, FeedCreateRequest request) {
    validateUserExists(request.authorId());
    validateWeatherExists(request.weatherId());

    Feed feed = Feed.create(request.authorId(), request.weatherId(), request.content());
    Feed savedFeed = feedRepository.save(feed);
    ootdService.create(savedFeed.getId(), request.clothesIds());

    eventPublisher.publishEvent(new FeedCreatedEvent()); // 캐시 무효화 이벤트

    return feedDtoAssembler.assemble(savedFeed, userId);
  }

  @CachePut(cacheNames="feeds", key="firstPage:createdAt")
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

    eventPublisher.publishEvent(new FeedDeletedEvent()); // 캐시 무효화 이벤트

    feedRepository.deleteById(feedId);
  }

  // 피드 목록 조회
  @Transactional(readOnly = true)
  @Cacheable(value = "feeds", key = "'firstPage:' +  #request.sortBy()", condition = "#request.cursor() == null && #request.idAfter() == null") // 첫 페이지만 캐싱
  public FeedDtoCursorResponse getFeeds(UUID currentUserId, FeedListRequest request){

    // 쿼리
    // 피드 불러오기
    List<Feed> feeds = feedRepositoryQueryDSL.getFeeds(request);
    int totalCount = feedRepositoryQueryDSL.countFeeds(request);

    // Dto 리스트로 변환
    List<FeedDto> feedDtos = feeds.stream().map(feed -> feedDtoAssembler.assemble(feed, currentUserId)).toList();

    // 반환
    // hasNext
    boolean hasNext = feedDtos.size() > request.limit();
    if (hasNext) {
      feedDtos = feedDtos.subList(0,request.limit());
    }

    // nextCursor, nextIdAfter
    String nextCursor = null;
    UUID nextIdAfter = null;
    FeedDto lastFeedDto = null;
    if (hasNext && feedDtos.size() >= request.limit()) {
      lastFeedDto = feedDtos.get(request.limit() - 1);

      if (request.sortBy().equals("createdAt")) {
        nextCursor = lastFeedDto.createdAt().toString();
      } else if (request.sortBy().equals("likeCount")) {
        nextCursor = String.valueOf(lastFeedDto.likeCount());
      }
      nextIdAfter = lastFeedDto.id();
    }


    // 최종 반환
    return new FeedDtoCursorResponse(feedDtos, nextCursor, nextIdAfter, hasNext, totalCount, request.sortBy(), request.sortDirection());

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
