package com.part4.team09.otboo.module.domain.feed.mapper;

import com.part4.team09.otboo.module.domain.feed.dto.CommentDto;
import com.part4.team09.otboo.module.domain.feed.entity.Comment;
import com.part4.team09.otboo.module.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapper {

  private final AuthorMapper authorMapper;

  public CommentDto toDto(Comment comment, User author) {
    return new CommentDto(
        comment.getId(),
        comment.getCreatedAt(),
        comment.getFeedId(),
        authorMapper.toDto(author),
        comment.getContent()
    );
  }
}
