package com.part4.team09.otboo.module.domain.feed.dto.request;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import java.util.UUID;

public record FeedListRequest(
    String cursor,
    UUID idAfter,
    int limit,
    String sortBy,
    SortDirection sortDirection,
    String keywordLike,
    Weather.SkyStatus skyStatusEqual,
    Precipitation.PrecipitationType precipitationTypeEqual,
    UUID authorIdEqual) {

}
