package com.part4.team09.otboo.module.domain.feed.dto.request;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

public record FeedListRequest(
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam(defaultValue = "20") @Min(value = 1, message = "limit은 0보다 커야합니다.") int limit,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESCENDING") SortDirection sortDirection,
        @RequestParam(required = false) String keywordLike,
        @RequestParam(required = false) Weather.SkyStatus skyStatusEqual,
        @RequestParam(required = false) Precipitation.PrecipitationType precipitationTypeEqual,
        @RequestParam(required = false) UUID authorIdEqual) {

}
