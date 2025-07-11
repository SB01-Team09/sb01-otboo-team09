package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.common.security.CustomUserDetails;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.exception.feed.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedDtoAssembler;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
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
  private final FeedDtoAssembler feedDtoAssembler;
  private final FeedRepositoryQueryDSL feedRepositoryQueryDSL;

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

  // 피드 목록 조회
  @Transactional(readOnly = true)
  public FeedDtoCursorResponse getFeeds(CustomUserDetails currentUser, String cursor, UUID idAfter, int limit, String sortBy, SortDirection sortDirection, String keywordLike, Weather.SkyStatus skyStatusEqual, Precipitation.PrecipitationType precipitationTypeEqual, UUID authorIdEqual){

    // 쿼리
    // 피드 불러오기
    List<Feed> feeds = feedRepositoryQueryDSL.getFeeds(cursor, idAfter, limit+1, sortBy, sortDirection, keywordLike, skyStatusEqual, precipitationTypeEqual, authorIdEqual);
    int totalCount = feedRepositoryQueryDSL.countFeeds(keywordLike, skyStatusEqual, precipitationTypeEqual, authorIdEqual);

    // Dto 리스트로 변환
    List<FeedDto> feedDtos = feeds.stream().map(feed -> feedDtoAssembler.assemble(feed, currentUser.getId())).toList();

    // 반환
    // hasNext
    boolean hasNext = feedDtos.size() > limit;
    if (hasNext) {
      feedDtos = feedDtos.subList(0, limit);
    }

    // nextCursor, nextIdAfter
    String nextCursor = null;
    UUID nextIdAfter = null;
    FeedDto lastFeedDto = feedDtos.get(limit-1);
    if(hasNext && !feedDtos.isEmpty()){
      if(sortBy == "createdAt"){
        nextCursor = lastFeedDto.createdAt().toString();
      }else if(sortBy == "likeCount"){
        nextCursor = String.valueOf(lastFeedDto.likeCount());
      }
      nextIdAfter = lastFeedDto.id();
    }

    // 최종 반환
    return new FeedDtoCursorResponse(feedDtos, nextCursor, nextIdAfter, hasNext, totalCount, sortBy, sortDirection);

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
