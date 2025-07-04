package com.part4.team09.otboo.module.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.domain.feed.dto.AuthorDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedUpdateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedMapper;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
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

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

  @Mock
  private FeedRepository feedRepository;

  @Mock
  private FeedMapper feedMapper;

  @Mock
  private OotdService ootdService;

  @Mock
  private LikeService likeService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private WeatherRepository weatherRepository;

  @InjectMocks
  private FeedService feedService;

  @Nested
  @DisplayName("피드 생성")
  public class CreateFeedTest {

    @Test
    @DisplayName("피드 생성 성공")
    void create_feed_success() {
      // given
      User mockUser = mock(User.class);
      Weather mockWeather = mock(Weather.class);
      Feed mockFeed = mock(Feed.class);
      AuthorDto mockAuthorDto = mock(AuthorDto.class);
      List<OotdDto> ootdDtos = List.of();

      FeedCreateRequest request = new FeedCreateRequest(
          UUID.randomUUID(),
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

      given(userRepository.findById(any())).willReturn(Optional.of(mockUser));
      given(weatherRepository.findById(any())).willReturn(Optional.of(mockWeather));
      given(feedRepository.save(any(Feed.class))).willReturn(mockFeed);
      given(ootdService.create(any(), any())).willReturn(ootdDtos);
      given(feedMapper.toDto(any(Feed.class), any(User.class), any(Weather.class), any(), eq(false))).willReturn(feedDto);

      // when
      FeedDto result = feedService.create(request);

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
      User mockUser = mock(User.class);
      Weather mockWeather = mock(Weather.class);
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

      given(userRepository.findById(any())).willReturn(Optional.of(mockUser));
      given(feedRepository.findById(any())).willReturn(Optional.of(mockFeed));
      given(weatherRepository.findById(any())).willReturn(Optional.of(mockWeather));
      given(ootdService.getOotds(any())).willReturn(List.of());
      given(likeService.isLikedByMe(any(), any())).willReturn(false);
      given(feedMapper.toDto(any(Feed.class), any(User.class), any(Weather.class), any(), eq(false))).willReturn(feedDto);

      // when
      FeedDto result = feedService.update(feedId, request);

      // then
      assertThat(result).isEqualTo(feedDto);
    }
  }
}
