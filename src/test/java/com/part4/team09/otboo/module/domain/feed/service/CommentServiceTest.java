package com.part4.team09.otboo.module.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.domain.feed.dto.AuthorDto;
import com.part4.team09.otboo.module.domain.feed.dto.CommentCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.CommentDto;
import com.part4.team09.otboo.module.domain.feed.entity.Comment;
import com.part4.team09.otboo.module.domain.feed.exception.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.mapper.CommentMapper;
import com.part4.team09.otboo.module.domain.feed.repository.CommentRepository;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
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
class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private CommentMapper commentMapper;

  @Mock
  private FeedRepository feedRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CommentService commentService;

  @Nested
  @DisplayName("댓글 생성")
  public class CreateCommentTest {

    @Test
    @DisplayName("댓글 생성 성공")
    void create_comment_success() {
      // given
      UUID feedId = UUID.randomUUID();
      Comment mockComment = mock(Comment.class);
      User mockUser = mock(User.class);
      AuthorDto mockAuthorDto = mock(AuthorDto.class);

      CommentCreateRequest request = new CommentCreateRequest(
          feedId,
          UUID.randomUUID(),
          "content"
      );

      CommentDto commentDto = new CommentDto(
          UUID.randomUUID(),
          LocalDateTime.now(),
          feedId,
          mockAuthorDto,
          "content"
      );

      given(userRepository.findById(any())).willReturn(Optional.of(mockUser));
      given(feedRepository.existsById(any())).willReturn(true);
      given(commentRepository.save(any(Comment.class))).willReturn(mockComment);
      given(commentMapper.toDto(any(Comment.class), any(User.class))).willReturn(commentDto);

      // when
      CommentDto result = commentService.create(request);

      // then
      assertThat(result).isEqualTo(commentDto);
      verify(commentRepository).save(any());
    }

    @Test
    @DisplayName("댓글 생성 실패 - 존재하지 않는 피드 ID")
    void create_comment_throwsFeedNotFoundException_whenFeedDoseNotExist() {
      // given
      UUID nonExistFeedId = UUID.randomUUID();

      CommentCreateRequest request = new CommentCreateRequest(
          nonExistFeedId,
          UUID.randomUUID(),
          "content"
      );

      given(feedRepository.existsById(nonExistFeedId)).willReturn(false);

      // when & then
      assertThrows(FeedNotFoundException.class,
          () -> commentService.create(request));
    }

    @Test
    @DisplayName("댓글 생성 실패 - 존재하지 않는 유저 ID")
    void create_comment_throwsUserNotFoundException_whenUserDoseNotExist() {
      // given
      UUID nonExistUserId = UUID.randomUUID();

      CommentCreateRequest request = new CommentCreateRequest(
          UUID.randomUUID(),
          nonExistUserId,
          "content"
      );

      given(feedRepository.existsById(any())).willReturn(true);
      given(userRepository.findById(nonExistUserId)).willReturn(Optional.empty());

      // when & then
      assertThrows(UserNotFoundException.class,
          () -> commentService.create(request));
    }
  }
}