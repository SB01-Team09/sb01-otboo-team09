package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.feed.document.FeedSearchDocument;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedListRequest;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.event.FeedCreatedEvent;
import com.part4.team09.otboo.module.domain.feed.event.FeedDeletedEvent;
import com.part4.team09.otboo.module.domain.feed.exception.feed.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedDtoAssembler;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.notification.event.FeedCreatedFollowerEvent;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherErrorCode;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherNotFoundException;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

  private final FeedRepository feedRepository;
  private final FeedDtoAssembler feedDtoAssembler;
  private final FeedRepositoryQueryDSL feedRepositoryQueryDSL;
  private final FeedSearchIndexService feedSearchIndexService;
  private final FeedSearchService feedSearchService;

  private final OotdService ootdService;

  private final CommentService commentService;

  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;
  private final LikeService likeService;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public FeedDto create(UUID userId, FeedCreateRequest request) {
    User author = getUserOrThrow(request.authorId());
    validateWeatherExists(request.weatherId());

    Feed feed = Feed.create(request.authorId(), request.weatherId(), request.content());
    Feed savedFeed = feedRepository.save(feed);
    ootdService.create(savedFeed.getId(), request.clothesIds());

    feedSearchIndexService.index(savedFeed); // 피드 생성시 OpenSearch 인덱싱

    eventPublisher.publishEvent(new FeedCreatedEvent()); // 캐시 무효화 이벤트
    eventPublisher.publishEvent(
        new FeedCreatedFollowerEvent(
            request.authorId(),
            author.getName(),
            request.content()
        )
    );

    return feedDtoAssembler.assemble(savedFeed, userId);
  }

  @CachePut(cacheNames="feeds", key="'firstPage:' +  #request.sortBy()")
  @PreAuthorize("@feedPermissionEvaluator.isFeedAuthor(principal.id, #feedId)")
  @Transactional
  public FeedDto update(UUID feedId, UUID userId, FeedUpdateRequest request) {
    Feed feed = getFeedOrThrow(feedId);
    feed.update(request.content());

    feedSearchIndexService.update(feed); // 피드 수정시 OpenSearch 인덱스 업데이트

    return feedDtoAssembler.assemble(feed, userId);
  }

  @PreAuthorize("hasRole('ADMIN') or @feedPermissionEvaluator.isFeedAuthor(principal.id, #feedId)")
  @Transactional
  public void delete(UUID feedId) {
    getFeedOrThrow(feedId);

    ootdService.deleteAllByFeedId(feedId);
    commentService.deleteAllByFeedId(feedId);
    likeService.deleteAllByFeedId(feedId);

    feedSearchIndexService.delete(feedId); // 피드 삭제시 OpenSearch 인덱스 삭제

    eventPublisher.publishEvent(new FeedDeletedEvent(feedId)); // 캐시 무효화 이벤트

    feedRepository.deleteById(feedId);
  }

  // 피드 목록 조회
  @Transactional(readOnly = true)
  @Cacheable(value = "feeds", key = "'firstPage:' +  #request.sortBy()", condition = "#request.cursor() == null && #request.idAfter() == null") // 첫 페이지만 캐싱
  public FeedDtoCursorResponse getFeeds(UUID currentUserId, FeedListRequest request){

    // 검색어 있을 때는 OpenSearch 사용
    boolean useOpenSearch = (
            (request.keywordLike() != null && !request.keywordLike().isBlank()) ||
                    request.skyStatusEqual() != null ||
                    request.precipitationTypeEqual() != null ||
                    request.authorIdEqual() != null
    );

    if (useOpenSearch) {
      List<FeedSearchDocument> documents = feedSearchService.searchWithFilters(request);

      List<UUID> feedIds = documents.stream()
              .map(FeedSearchDocument::getId)
              .toList();

      List<Feed> feeds = feedRepository.findAllById(feedIds);

      Map<UUID, Feed> feedMap = feeds.stream()
              .collect(Collectors.toMap(Feed::getId, Function.identity()));

      List<FeedDto> feedDtos = feedIds.stream()
              .map(feedMap::get)
              .filter(Objects::nonNull)
              .map(feed -> feedDtoAssembler.assemble(feed, currentUserId))
              .toList();

      return new FeedDtoCursorResponse(
              feedDtos,
              null, // TODO: OpenSearch 페이징 처리
              null,
              false,
              feedDtos.size(),
              request.sortBy(),
              request.sortDirection()
      );
    }

    // 검색어 없으면 기존 DB 쿼리 방식 유지
    List<Feed> feeds = feedRepositoryQueryDSL.getFeeds(request);
    int totalCount = feedRepositoryQueryDSL.countFeeds(request);

    List<FeedDto> feedDtos = feeds.stream()
            .map(feed -> feedDtoAssembler.assemble(feed, currentUserId))
            .toList();

    boolean hasNext = feedDtos.size() > request.limit();
    if (hasNext) {
      feedDtos = feedDtos.subList(0, request.limit());
    }

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && feedDtos.size() >= request.limit()) {
      FeedDto lastFeedDto = feedDtos.get(request.limit() - 1);
      if (request.sortBy().equals("createdAt")) {
        nextCursor = lastFeedDto.createdAt().toString();
      } else if (request.sortBy().equals("likeCount")) {
        nextCursor = String.valueOf(lastFeedDto.likeCount());
      }
      nextIdAfter = lastFeedDto.id();
    }

    return new FeedDtoCursorResponse(
            feedDtos,
            nextCursor,
            nextIdAfter,
            hasNext,
            totalCount,
            request.sortBy(),
            request.sortDirection()
    );
  }

  private Feed getFeedOrThrow(UUID feedId) {
    return feedRepository.findById(feedId)
        .orElseThrow(() -> FeedNotFoundException.withId(feedId));
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
  }

  private void validateWeatherExists(UUID weatherId) {
    if (!weatherRepository.existsById(weatherId)) {
      throw WeatherNotFoundException.withId(WeatherErrorCode.WEATHER_NOF_FOUND, weatherId);
    }
  }
}
