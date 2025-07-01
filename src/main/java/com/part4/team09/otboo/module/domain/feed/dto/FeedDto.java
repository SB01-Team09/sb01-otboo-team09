package com.part4.team09.otboo.module.domain.feed.dto;

import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FeedDto(
    UUID id,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    AuthorDto authorDto,
    // TODO: WeatherSummaryDto 로 변경
    Weather weather,
    List<OotdDto> ootds,
    String content,
    int likeCount,
    int commentCount,
    boolean likedByMe
) {

}
