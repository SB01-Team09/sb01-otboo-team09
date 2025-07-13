package com.part4.team09.otboo.module.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.part4.team09.otboo.module.domain.feed.dto.AuthorDto;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.event.FeedCreatedEvent;
import com.part4.team09.otboo.module.domain.feed.event.FeedDeletedEvent;
import com.part4.team09.otboo.module.domain.feed.event.FeedUpdatedEvent;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedDtoAssembler;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.follow.event.FollowCreatedEvent;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherSummaryDto;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
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
      doNothing().when(eventPublisher).publishEvent(any(FeedUpdatedEvent.class));

      // when
      FeedDto result = feedService.update(feedId, userId, request);

      // then
      assertThat(result).isEqualTo(feedDto);
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
