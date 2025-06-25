package com.part4.team09.otboo.module.domain.feed.dto;

import java.util.UUID;

public record FeedCreateRequest(
    UUID authorId,
    UUID weatherId,
    UUID clothesIds,
    String content
) {

}
