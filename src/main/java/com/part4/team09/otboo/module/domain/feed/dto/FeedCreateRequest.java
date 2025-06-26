package com.part4.team09.otboo.module.domain.feed.dto;

import java.util.List;
import java.util.UUID;

public record FeedCreateRequest(
    UUID authorId,
    UUID weatherId,
    List<UUID> clothesIds,
    String content
) {

}
