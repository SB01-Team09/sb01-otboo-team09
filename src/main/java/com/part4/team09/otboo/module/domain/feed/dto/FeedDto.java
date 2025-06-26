package com.part4.team09.otboo.module.domain.feed.dto;

import com.part4.team09.otboo.module.domain.feed.entity.Ootd;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FeedDto(
    UUID id,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    AuthorDto authorDto,
    Weather weather,
    List<Ootd> ootds,
    String content,
    int likeCount,
    int commentCount,
    boolean likedByMe
) {
}
