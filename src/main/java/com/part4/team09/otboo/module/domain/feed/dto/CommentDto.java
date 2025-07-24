package com.part4.team09.otboo.module.domain.feed.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDto(
    UUID id,
    LocalDateTime createdAt,
    UUID feedId,
    AuthorDto author,
    String content
) {

}
