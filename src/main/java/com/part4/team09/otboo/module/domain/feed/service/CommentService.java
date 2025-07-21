package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.feed.dto.CommentDtoCursorResponse;
import com.part4.team09.otboo.module.domain.feed.dto.request.CommentCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.CommentDto;
import com.part4.team09.otboo.module.domain.feed.entity.Comment;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.event.CommentCreatedEvent;
import com.part4.team09.otboo.module.domain.feed.event.CommentDeletedEvent;
import com.part4.team09.otboo.module.domain.feed.exception.feed.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.mapper.CommentMapper;
import com.part4.team09.otboo.module.domain.feed.repository.CommentRepository;
import com.part4.team09.otboo.module.domain.feed.repository.CommentRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.follow.event.FollowCreatedEvent;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final CommentRepositoryQueryDSL commentRepositoryQueryDSL;
  private final CommentMapper commentMapper;

  private final FeedRepository feedRepository;
  private final UserRepository userRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public CommentDto create(UUID feedId, CommentCreateRequest request) {
    Feed feed = getFeedOrThrow(feedId);
    User author = getUserOrThrow(request.authorId());

    Comment comment = Comment.create(feedId, request.authorId(), request.content());
    Comment savedComment = commentRepository.save(comment);
    feed.increaseCommentCount();

    eventPublisher.publishEvent(new CommentCreatedEvent(feedId));

    return commentMapper.toDto(savedComment, author);
  }

  // 댓글 목록 조회
  @Transactional(readOnly = true)
  @Cacheable(value = "comments", key = "#feedId", condition = "#cursor == null && #idAfter == null") // 첫 페이지만 캐싱
  public CommentDtoCursorResponse getComments(UUID feedId, String cursor, UUID idAfter, int limit){

    // 쿼리
    // 댓글 불러오기
    List<Comment> comments = commentRepositoryQueryDSL.getComments(feedId, cursor, idAfter, limit+1);
    int totalCount = commentRepositoryQueryDSL.countComments(feedId);

    Feed feed = feedRepository.findById(feedId).orElseThrow();
    User author = userRepository.findById(feed.getAuthorId()).orElseThrow();

    // Dto 리스트로 변환
    List<CommentDto> commentDtos = comments.stream().map(comment -> commentMapper.toDto(comment, author)).toList();

    // 반환
    // hasNext
    boolean hasNext = commentDtos.size() > limit;
    if (hasNext) {
      commentDtos = commentDtos.subList(0, limit);
    }

    // nextCursor, nextIdAfter
    String nextCursor = null;
    UUID nextIdAfter = null;
    CommentDto lastCommentDto = null;
    if (hasNext && commentDtos.size() >= limit) {
      lastCommentDto = commentDtos.get(limit - 1);
      nextCursor = lastCommentDto.createdAt().toString();
      nextIdAfter = lastCommentDto.id();
    }

    // 최종 반환
    return new CommentDtoCursorResponse(commentDtos, nextCursor, nextIdAfter, hasNext, totalCount, "createdAt", SortDirection.ASCENDING);
  }

  @Transactional
  public void deleteAllByFeedId(UUID feedId) {
    commentRepository.deleteAllByFeedId(feedId);

    eventPublisher.publishEvent(new CommentDeletedEvent(feedId));
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
  }

  private Feed getFeedOrThrow(UUID feedId) {
    return feedRepository.findById(feedId)
        .orElseThrow(() -> FeedNotFoundException.withId(feedId));
  }
}
