package com.part4.team09.otboo.module.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.domain.feed.dto.AuthorDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.entity.Like;
import com.part4.team09.otboo.module.domain.feed.exception.feed.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.exception.like.LikeNotFoundException;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedDtoAssembler;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.feed.repository.LikeRepository;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherSummaryDto;
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
class LikeServiceTest {

  @Mock
  private LikeRepository likeRepository;

  @Mock
  private FeedDtoAssembler feedDtoAssembler;

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
      WeatherSummaryDto mockWeather = mock(WeatherSummaryDto.class);
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

      given(feedRepository.existsById(any())).willReturn(true);
      given(userRepository.existsById(any())).willReturn(true);
      given(feedDtoAssembler.assemble(feedId, userId)).willReturn(feedDto);

      // when
      FeedDto result = likeService.create(userId, feedId);

      // then
      assertThat(result).isEqualTo(feedDto);
      verify(likeRepository).save(any());
    }

    @Test
    @DisplayName("좋아요 생성 실패 - 존재하지 않는 피드 ID")
    void create_like_throwsFeedNotFoundException_whenFeedDoseNotExist() {
      // given
      UUID nonExistFeedId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      given(feedRepository.existsById(nonExistFeedId)).willReturn(false);

      // when & then
      assertThrows(FeedNotFoundException.class,
          () -> likeService.create(userId, nonExistFeedId));
    }

    @Test
    @DisplayName("좋아요 생성 실패 - 존재하지 않는 유저 ID")
    void create_like_throwsUserNotFoundException_whenUserDoseNotExist() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID nonExistUserId = UUID.randomUUID();

      given(feedRepository.existsById(feedId)).willReturn(true);
      given(userRepository.existsById(nonExistUserId)).willReturn(false);

      // when & then
      assertThrows(UserNotFoundException.class,
          () -> likeService.create(nonExistUserId, feedId));
    }
  }

  @Nested
  @DisplayName("좋아요 삭제")
  public class DeleteLikeTest {

    @Test
    @DisplayName("좋아요 삭제 성공")
    void delete_like_success() {
      // given
      UUID userId = UUID.randomUUID();
      UUID feedId = UUID.randomUUID();
      UUID likeId = UUID.randomUUID();
      Like mockLike = mock(Like.class);

      given(feedRepository.existsById(any())).willReturn(true);
      given(userRepository.existsById(any())).willReturn(true);
      given(likeRepository.findByUserIdAndFeedId(userId, feedId)).willReturn(Optional.of(mockLike));
      given(mockLike.getId()).willReturn(likeId);

      // when
      likeService.delete(userId, feedId);

      // then
      verify(likeRepository).deleteById(likeId);
    }

    @Test
    @DisplayName("좋아요 삭제 실패 - 존재하지 않는 피드 ID")
    void delete_like_throwsFeedNotFoundException_whenFeedDoseNotExist() {
      // given
      UUID nonExistFeedId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      given(feedRepository.existsById(nonExistFeedId)).willReturn(false);

      // when & then
      assertThrows(FeedNotFoundException.class,
          () -> likeService.delete(userId, nonExistFeedId));
    }

    @Test
    @DisplayName("좋아요 삭제 실패 - 존재하지 않는 유저 ID")
    void delete_like_throwsUserNotFoundException_whenUserDoseNotExist() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID nonExistUserId = UUID.randomUUID();

      given(feedRepository.existsById(feedId)).willReturn(true);
      given(userRepository.existsById(nonExistUserId)).willReturn(false);

      // when & then
      assertThrows(UserNotFoundException.class,
          () -> likeService.delete(nonExistUserId, feedId));
    }

    @Test
    @DisplayName("좋아요 삭제 실패 - 존재하지 않는 좋아요")
    void delete_like_throwsLikeNotFoundException_whenLikeDoseNotExist() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      given(feedRepository.existsById(feedId)).willReturn(true);
      given(userRepository.existsById(userId)).willReturn(true);
      given(likeRepository.findByUserIdAndFeedId(userId, feedId)).willReturn(Optional.empty());

      // when & then
      assertThrows(LikeNotFoundException.class,
          () -> likeService.delete(userId, feedId));
    }
  }
}