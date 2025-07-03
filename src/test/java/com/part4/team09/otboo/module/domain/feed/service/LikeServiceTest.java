package com.part4.team09.otboo.module.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.part4.team09.otboo.module.domain.feed.dto.AuthorDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

  @Mock
  private FeedRepository feedRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private LikeService likeService;

  @Nested
  @DisplayName("좋아요 생성")
  public class CreateLikeTest {

    @Test
    @DisplayName("좋아요 생성 성공")
    void create_like_success() {
      // given
      UUID userId = UUID.randomUUID();
      UUID feedId = UUID.randomUUID();

      Weather mockWeather = mock(Weather.class);
      AuthorDto mockAuthorDto = mock(AuthorDto.class);

      FeedDto feedDto = new FeedDto(
          feedId,
          LocalDateTime.now(),
          LocalDateTime.now(),
          mockAuthorDto,
          mockWeather,
          List.of(),
          "content",
          0,
          0,
          true
      );

      // when
      FeedDto result = likeService.create(userId, feedId);

      // then
      assertThat(result).isEqualTo(feedDto);
    }
  }

}