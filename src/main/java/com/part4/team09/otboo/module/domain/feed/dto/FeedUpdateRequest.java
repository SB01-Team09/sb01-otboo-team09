package com.part4.team09.otboo.module.domain.feed.dto;

import jakarta.validation.constraints.NotBlank;

public record FeedUpdateRequest(

    @NotBlank(message = "content는 비어 있을 수 없습니다.")
    String content
) {

}
