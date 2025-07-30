package com.part4.team09.otboo.module.domain.feed.dto;

import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherSummaryDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FeedDto(
    UUID id,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    AuthorDto author,
    WeatherSummaryDto weather,
    List<OotdDto> ootds,
    String content,
    int likeCount,
    int commentCount,
    boolean likedByMe
) {

}
