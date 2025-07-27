package com.part4.team09.otboo.module.domain.feed.mapper;

import com.part4.team09.otboo.module.domain.feed.document.FeedSearchDocument;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherErrorCode;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherNotFoundException;
import com.part4.team09.otboo.module.domain.weather.repository.PrecipitationRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedSearchDocumentAssembler {

    private final WeatherRepository weatherRepository;
    private final PrecipitationRepository precipitationRepository;

    public FeedSearchDocument toDocument(Feed feed) {
        Weather weather = weatherRepository.findById(feed.getWeatherId())
                .orElseThrow(() -> WeatherNotFoundException.withId(WeatherErrorCode.WEATHER_NOF_FOUND, feed.getWeatherId()));

        Precipitation precipitation = precipitationRepository.findById(weather.getPrecipitationId())
                .orElseThrow(() -> WeatherNotFoundException.withId(WeatherErrorCode.PRECIPITATION_NOF_FOUND, weather.getPrecipitationId()));

        return FeedSearchDocument.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .createdAt(feed.getCreatedAt())
                .likeCount(feed.getLikeCount())
                .weather(FeedSearchDocument.WeatherDocument.builder()
                        .skyStatus(weather.getSkyStatus())
                        .precipitationType(precipitation.getType())
                        .build())
                .build();
    }
}