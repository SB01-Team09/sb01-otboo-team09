package com.part4.team09.otboo.module.domain.feed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommentCreateRequest(

    @NotNull(message = "feedId는 필수입니다.")
    UUID feedId,

    @NotNull(message = "authorId는 필수입니다.")
    UUID authorId,

    @NotBlank(message = "content는 비어 있을 수 없습니다.")
    String content
) {

}
