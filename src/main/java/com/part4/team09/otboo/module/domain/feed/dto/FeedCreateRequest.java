package com.part4.team09.otboo.module.domain.feed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record FeedCreateRequest(

    @NotNull(message = "authorId는 필수입니다.")
    UUID authorId,

    @NotNull(message = "weatherId는 필수입니다.")
    UUID weatherId,

    @NotEmpty(message = "clothesIds는 하나 이상 필요합니다.")
    List<UUID> clothesIds,

    @NotBlank(message = "content는 비어 있을 수 없습니다.")
    String content
) {

}
