package com.part4.team09.otboo.module.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.feed.dto.AuthorDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedListRequest;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.event.FeedCreatedEvent;
import com.part4.team09.otboo.module.domain.feed.event.FeedDeletedEvent;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedDtoAssembler;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherSummaryDto;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

  @Mock
  private FeedRepository feedRepository;

  @Mock
  private FeedRepositoryQueryDSL feedRepositoryQueryDSL;

  @Mock
  private FeedDtoAssembler feedDtoAssembler;

  @Mock
  private OotdService ootdService;

  @Mock
  private LikeService likeService;

  @Mock
  private CommentService commentService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private WeatherRepository weatherRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private FeedService feedService;

  @Nested
  @DisplayName("피드 생성")
  public class CreateFeedTest {

    @Test
    @DisplayName("피드 생성 성공")
    void create_feed_success() {
      // given
      UUID userId = UUID.randomUUID();
      WeatherSummaryDto mockWeather = mock(WeatherSummaryDto.class);
      Feed mockFeed = mock(Feed.class);
      AuthorDto mockAuthorDto = mock(AuthorDto.class);
      List<OotdDto> ootdDtos = List.of();

      FeedCreateRequest request = new FeedCreateRequest(
          userId,
          UUID.randomUUID(),
          List.of(),
          "content"
      );

      FeedDto feedDto = new FeedDto(
          UUID.randomUUID(),
          LocalDateTime.now(),
          LocalDateTime.now(),
          mockAuthorDto,
          mockWeather,
          ootdDtos,
          "content",
          0,
          0,
          false
      );

      given(userRepository.existsById(any())).willReturn(true);
      given(weatherRepository.existsById(any())).willReturn(true);
      given(feedRepository.save(any(Feed.class))).willReturn(mockFeed);
      given(feedDtoAssembler.assemble(any(Feed.class), eq(userId))).willReturn(feedDto);
      doNothing().when(eventPublisher).publishEvent(any(FeedCreatedEvent.class));

      // when
      FeedDto result = feedService.create(userId, request);

      // then
      assertThat(result).isEqualTo(feedDto);
      verify(feedRepository).save(any());
    }
  }

  @Nested
  @DisplayName("피드 수정")
  public class UpdateFeedTest {

    @Test
    @DisplayName("피드 수정 성공")
    void update_feed_success() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      WeatherSummaryDto mockWeather = mock(WeatherSummaryDto.class);
      Feed mockFeed = mock(Feed.class);
      AuthorDto mockAuthorDto = mock(AuthorDto.class);

      FeedUpdateRequest request = new FeedUpdateRequest("newContent");

      FeedDto feedDto = new FeedDto(
          feedId,
          LocalDateTime.now(),
          LocalDateTime.now(),
          mockAuthorDto,
          mockWeather,
          List.of(),
          "newContent",
          0,
          0,
          false
      );

      given(feedRepository.findById(any())).willReturn(Optional.of(mockFeed));
      given(feedDtoAssembler.assemble(any(Feed.class), eq(userId))).willReturn(feedDto);

      // when
      FeedDto result = feedService.update(feedId, userId, request);

      // then
      assertThat(result).isEqualTo(feedDto);
    }
  }

  @Nested
  @DisplayName("피드 목록 조회")
  class GetFeedsTest {

    @Test
    @DisplayName("피드 목록 조회 성공 - OOTD 포함 여부 확인")
    void get_feeds_ootds_included() {
      // given
      UUID userId = UUID.randomUUID();
      FeedListRequest request = new FeedListRequest(null, null, 10, "createdAt",
        SortDirection.DESCENDING, null, null, null, null);

      UUID clothesId = UUID.randomUUID();
      OotdDto ootdDto = new OotdDto(clothesId, "상의", null, Clothes.ClothesType.TOP, null);
      FeedDto feedDto = new FeedDto(
        UUID.randomUUID(),
        LocalDateTime.now(),
        LocalDateTime.now(),
        mock(AuthorDto.class),
        mock(WeatherSummaryDto.class),
        List.of(ootdDto),
        "내용",
        5,
        3,
        false
      );

      Feed feed = mock(Feed.class);
      given(feedRepositoryQueryDSL.getFeeds(request)).willReturn(List.of(feed));
      given(feedRepositoryQueryDSL.countFeeds(request)).willReturn(1);
      given(feedDtoAssembler.assemble(eq(feed), eq(userId))).willReturn(feedDto);

      // when
      FeedDtoCursorResponse response = feedService.getFeeds(userId, request);

      // then
      assertThat(response.data()).hasSize(1);

      FeedDto dto = response.data().get(0);
      assertThat(dto.ootds()).isNotEmpty();
      assertThat(dto.ootds()).anyMatch(o -> o.clothesId().equals(clothesId));
    }

    @Test
    @DisplayName("피드 목록 조회 성공 - 페이징")
    void get_feeds_success() {
      // given
      UUID userId = UUID.randomUUID();
      FeedListRequest request = new FeedListRequest(
        null,
        null,
        2,
        "createdAt",
        SortDirection.DESCENDING,
        null,
        null,
        null,
        null
      );

      Feed feed1 = mock(Feed.class);
      Feed feed2 = mock(Feed.class);

      List<Feed> feedEntities = List.of(feed1, feed2);
      List<FeedDto> feedDtos = List.of(
        new FeedDto(UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(),
          mock(AuthorDto.class), mock(WeatherSummaryDto.class), List.of(), "content1", 3, 1, false),
        new FeedDto(UUID.randomUUID(), LocalDateTime.now().minusMinutes(1), LocalDateTime.now(),
          mock(AuthorDto.class), mock(WeatherSummaryDto.class), List.of(), "content2", 1, 2, false)
      );

      given(feedRepositoryQueryDSL.getFeeds(request)).willReturn(feedEntities);
      given(feedRepositoryQueryDSL.countFeeds(request)).willReturn(10);
      given(feedDtoAssembler.assemble(eq(feed1), eq(userId))).willReturn(feedDtos.get(0));
      given(feedDtoAssembler.assemble(eq(feed2), eq(userId))).willReturn(feedDtos.get(1));

      // when
      FeedDtoCursorResponse result = feedService.getFeeds(userId, request);

      // then
      assertThat(result).isNotNull();
      assertThat(result.data()).hasSize(2);
      assertThat(result.hasNext()).isFalse(); // limit과 개수 같음 → hasNext false
      assertThat(result.totalCount()).isEqualTo(10);
      assertThat(result.sortBy()).isEqualTo("createdAt");
      assertThat(result.sortDirection()).isEqualTo(SortDirection.DESCENDING);

      verify(feedRepositoryQueryDSL).getFeeds(request);
    }

    @Test
    @DisplayName("피드 목록 조회 성공 - hasNext = true")
    void get_feeds_hasNext_true() {
      // given
      UUID userId = UUID.randomUUID();
      FeedListRequest request = new FeedListRequest(null, null, 1, "likeCount",
        SortDirection.ASCENDING, null, null, null, null);

      Feed feed1 = mock(Feed.class);
      Feed feed2 = mock(Feed.class);

      List<Feed> feedEntities = List.of(feed1, feed2); // 2개를 넘겨 limit보다 많음
      List<FeedDto> feedDtos = List.of(
        new FeedDto(UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(),
          mock(AuthorDto.class), mock(WeatherSummaryDto.class), List.of(), "content1", 5, 0, false),
        new FeedDto(UUID.randomUUID(), LocalDateTime.now().minusHours(1), LocalDateTime.now(),
          mock(AuthorDto.class), mock(WeatherSummaryDto.class), List.of(), "content2", 4, 0, false)
      );

      given(feedRepositoryQueryDSL.getFeeds(request)).willReturn(feedEntities);
      given(feedRepositoryQueryDSL.countFeeds(request)).willReturn(100);
      given(feedDtoAssembler.assemble(eq(feed1), eq(userId))).willReturn(feedDtos.get(0));
      given(feedDtoAssembler.assemble(eq(feed2), eq(userId))).willReturn(feedDtos.get(1));

      // when
      FeedDtoCursorResponse result = feedService.getFeeds(userId, request);

      // then
      assertThat(result.hasNext()).isTrue();
      assertThat(result.data()).hasSize(1); // hasNext true → 하나만 반환
      assertThat(result.nextCursor()).isEqualTo("5"); // likeCount 기준
      assertThat(result.nextIdAfter()).isEqualTo(feedDtos.get(0).id());
    }
  }


  @Nested
  @DisplayName("피드 삭제")
  public class DeleteFeedTest {

    @Test
    @DisplayName("피드 삭제 성공")
    void delete_feed_success() {
      // given
      UUID feedId = UUID.randomUUID();

      given(feedRepository.existsById(feedId)).willReturn(true);
      doNothing().when(eventPublisher).publishEvent(any(FeedDeletedEvent.class));

      // when
      feedService.delete(feedId);

      // then
      verify(ootdService).deleteAllByFeedId(feedId);
      verify(commentService).deleteAllByFeedId(feedId);
      verify(likeService).deleteAllByFeedId(feedId);
      verify(feedRepository).deleteById(feedId);
    }
  }
}
