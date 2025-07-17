package com.part4.team09.otboo.module.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.feed.dto.AuthorDto;
import com.part4.team09.otboo.module.domain.feed.dto.CommentDto;
import com.part4.team09.otboo.module.domain.feed.dto.CommentDtoCursorResponse;
import com.part4.team09.otboo.module.domain.feed.dto.request.CommentCreateRequest;
import com.part4.team09.otboo.module.domain.feed.entity.Comment;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.event.CommentCreatedEvent;
import com.part4.team09.otboo.module.domain.feed.exception.feed.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.mapper.CommentMapper;
import com.part4.team09.otboo.module.domain.feed.repository.CommentRepository;
import com.part4.team09.otboo.module.domain.feed.repository.CommentRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
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
class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private CommentRepositoryQueryDSL commentRepositoryQueryDSL;

  @Mock
  private CommentMapper commentMapper;

  @Mock
  private FeedRepository feedRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

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
      doNothing().when(eventPublisher).publishEvent(any(CommentCreatedEvent.class));

      // when
      CommentDto result = commentService.create(feedId, request);

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
          () -> commentService.create(nonExistFeedId, request));
    }

    @Test
    @DisplayName("댓글 생성 실패 - 존재하지 않는 유저 ID")
    void create_comment_throwsUserNotFoundException_whenUserDoseNotExist() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID nonExistUserId = UUID.randomUUID();

      CommentCreateRequest request = new CommentCreateRequest(
          feedId,
          nonExistUserId,
          "content"
      );

      given(feedRepository.existsById(any())).willReturn(true);
      given(userRepository.findById(nonExistUserId)).willReturn(Optional.empty());

      // when & then
      assertThrows(UserNotFoundException.class,
          () -> commentService.create(feedId, request));
    }
  }

  @Test
  @DisplayName("댓글 목록 조회 성공")
  void get_comments_success() {
    // given
    UUID feedId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();

    int limit = 10;
    String cursor = null;
    UUID idAfter = null;

    Feed mockFeed = mock(Feed.class);
    given(mockFeed.getAuthorId()).willReturn(authorId);

    User mockAuthor = mock(User.class);
    Comment comment = Comment.create(feedId, authorId, "댓글1");
    CommentDto commentDto = new CommentDto(
      commentId,
      LocalDateTime.now(),
      feedId,
      mock(AuthorDto.class),
      comment.getContent()
    );

    List<Comment> commentEntities = List.of(comment);
    List<CommentDto> commentDtoList = List.of(commentDto);

    // mock 처리
    given(feedRepository.findById(feedId)).willReturn(Optional.of(mockFeed));
    given(userRepository.findById(authorId)).willReturn(Optional.of(mockAuthor));
    given(commentRepositoryQueryDSL.getComments(feedId, cursor, idAfter, limit + 1)).willReturn(
      commentEntities);
    given(commentRepositoryQueryDSL.countComments(feedId)).willReturn(1);
    given(commentMapper.toDto(comment, mockAuthor)).willReturn(commentDto);

    // when
    CommentDtoCursorResponse response = commentService.getComments(feedId, cursor, idAfter, limit);

    // then
    assertThat(response.data()).hasSize(1);
    assertThat(response.totalCount()).isEqualTo(1);
    assertThat(response.hasNext()).isFalse();
    assertThat(response.sortBy()).isEqualTo("createdAt");
    assertThat(response.sortDirection()).isEqualTo(SortDirection.ASCENDING);

    CommentDto resultDto = response.data().get(0);
    assertThat(resultDto.id()).isEqualTo(commentId);
    assertThat(resultDto.feedId()).isEqualTo(feedId);
    assertThat(resultDto.content()).isEqualTo("댓글1");

    verify(commentRepositoryQueryDSL).getComments(feedId, cursor, idAfter, limit + 1);
    verify(commentRepositoryQueryDSL).countComments(feedId);
    verify(feedRepository).findById(feedId);
    verify(userRepository).findById(authorId);
    verify(commentMapper).toDto(comment, mockAuthor);
  }
}