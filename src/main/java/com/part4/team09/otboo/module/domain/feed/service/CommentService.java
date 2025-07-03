package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.feed.dto.CommentCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.CommentDto;
import com.part4.team09.otboo.module.domain.feed.entity.Comment;
import com.part4.team09.otboo.module.domain.feed.mapper.CommentMapper;
import com.part4.team09.otboo.module.domain.feed.repository.CommentRepository;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final CommentMapper commentMapper;

  private final FeedRepository feedRepository;
  private final UserRepository userRepository;

  @Transactional
  public CommentDto create(CommentCreateRequest request) {
    validateFeedExists(request.feedId());
    User author = getUserOrThrow(request.authorId());

    Comment comment = Comment.create(request.feedId(), request.authorId(), request.content());
    Comment savedComment = commentRepository.save(comment);

    return commentMapper.toDto(savedComment, author);
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
  }

  private void validateFeedExists(UUID feedId) {
    if (!feedRepository.existsById(feedId)) {
      // TODO: 피드 커스텀 예외로 변경
      throw new IllegalArgumentException();
    }
  }
}
