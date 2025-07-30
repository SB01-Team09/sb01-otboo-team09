package com.part4.team09.otboo.module.domain.feed.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
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
class FeedPermissionEvaluatorTest {

  @Mock
  private FeedRepository feedRepository;

  @InjectMocks
  private FeedPermissionEvaluator feedPermissionEvaluator;

  @Nested
  @DisplayName("피드 작성자 확인")
  public class isFeedAuthorTest {

    @Test
    @DisplayName("피드 작성자 확인 - true")
    void isFeedAuthor_true() {
      // given
      UUID userId = UUID.randomUUID();
      UUID feedId = UUID.randomUUID();
      Feed mockFeed = mock(Feed.class);

      given(feedRepository.findById(feedId)).willReturn(Optional.of(mockFeed));
      given(mockFeed.getAuthorId()).willReturn(userId);

      // when
      Boolean result = feedPermissionEvaluator.isFeedAuthor(userId, feedId);

      // then
      assertThat(result).isEqualTo(true);
      verify(feedRepository).findById(feedId);
    }

    @Test
    @DisplayName("피드 작성자 확인 - false : 피드 작성자가 아님")
    void isFeedAuthor_false_whenUserIsNotAuthor() {
      // given
      UUID userId1 = UUID.randomUUID();
      UUID userId2 = UUID.randomUUID();
      UUID feedId = UUID.randomUUID();
      Feed mockFeed = mock(Feed.class);

      given(feedRepository.findById(feedId)).willReturn(Optional.of(mockFeed));
      given(mockFeed.getAuthorId()).willReturn(userId2);

      // when
      Boolean result = feedPermissionEvaluator.isFeedAuthor(userId1, feedId);

      // then
      assertThat(result).isEqualTo(false);
      verify(feedRepository).findById(feedId);
    }

    @Test
    @DisplayName("피드 작성자 확인 - false : 피드가 없음")
    void isFeedAuthor_false_whenFeedNotFound() {
      // given
      UUID userId = UUID.randomUUID();
      UUID feedId = UUID.randomUUID();

      given(feedRepository.findById(feedId)).willReturn(Optional.empty());

      // when
      Boolean result = feedPermissionEvaluator.isFeedAuthor(userId, feedId);

      // then
      assertThat(result).isEqualTo(false);
      verify(feedRepository).findById(feedId);
    }
  }
}