package com.part4.team09.otboo.module.domain.feed.document;

import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedSearchDocument {
    private UUID id;
    private String content;
    private WeatherDocument weather;
    private LocalDateTime createdAt;
    private int likeCount;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WeatherDocument {
        private Weather.SkyStatus skyStatus;
        private Precipitation.PrecipitationType precipitationType;
    }
}