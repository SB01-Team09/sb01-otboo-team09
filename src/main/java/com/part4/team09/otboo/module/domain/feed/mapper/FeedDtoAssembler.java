package com.part4.team09.otboo.module.domain.feed.mapper;

import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.exception.feed.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.feed.repository.LikeRepository;
import com.part4.team09.otboo.module.domain.feed.service.OotdService;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherSummaryDto;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherErrorCode;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherNotFoundException;
import com.part4.team09.otboo.module.domain.weather.mapper.WeatherMapper;
import com.part4.team09.otboo.module.domain.weather.repository.PrecipitationRepository;
import com.part4.team09.otboo.module.domain.weather.repository.TemperatureRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedDtoAssembler {

  private final FeedMapper feedMapper;
  private final WeatherMapper weatherMapper;

  private final OotdService ootdService;

  private final FeedRepository feedRepository;
  private final LikeRepository likeRepository;
  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;
  private final PrecipitationRepository precipitationRepository;
  private final TemperatureRepository temperatureRepository;

  public FeedDto assemble(UUID feedId, UUID userId) {
    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> {
          log.warn("피드를 찾을 수 없습니다. feedId: {}", feedId);
          return FeedNotFoundException.withId(feedId);
        });

    return assemble(feed, userId);
  }

  public FeedDto assemble(Feed feed, UUID userId) {
    log.debug("피드 DTO assemble - feedId: {}, userId: {}", feed.getId(), userId);

    User author = userRepository.findById(feed.getAuthorId())
        .orElseThrow(() -> {
          log.warn("작성자를 찾을 수 없습니다. authorId: {}", feed.getAuthorId());
          return UserNotFoundException.withId(feed.getAuthorId());
        });

    Weather weather = weatherRepository.findById(feed.getWeatherId())
        .orElseThrow(() -> {
          log.warn("날씨 정보를 찾을 수 없습니다. weatherId: {}", feed.getWeatherId());
          return WeatherNotFoundException.withId(
              WeatherErrorCode.WEATHER_NOF_FOUND,
              feed.getWeatherId()
          );
        });

    WeatherSummaryDto weatherSummary = getWeatherSummaryDto(weather);
    List<OotdDto> ootds = ootdService.getOotds(feed.getId());
    boolean likedByMe = likeRepository.existsByUserIdAndFeedId(userId, feed.getId());

    log.debug("FeedDto assemble 완료 - feedId: {}, OOTD 개수: {}, 좋아요 여부: {}",
        feed.getId(), ootds.size(), likedByMe);

    return feedMapper.toDto(feed, author, weatherSummary, ootds, likedByMe);
  }

  private WeatherSummaryDto getWeatherSummaryDto(Weather weather) {
    log.debug("날씨 요약 정보 조회 시작 - weatherId: {}", weather.getId());

    Precipitation precipitation = precipitationRepository.findById(weather.getPrecipitationId())
        .orElseThrow(() -> {
          log.warn("강수 정보를 찾을 수 없습니다. precipitationId: {}", weather.getPrecipitationId());
          return WeatherNotFoundException.withId(
              WeatherErrorCode.PRECIPITATION_NOF_FOUND,
              weather.getPrecipitationId()
          );
        });

    Temperature temperature = temperatureRepository.findById(weather.getTemperatureId())
        .orElseThrow(() -> {
          log.warn("기온 정보를 찾을 수 없습니다. temperatureId: {}", weather.getTemperatureId());
          return WeatherNotFoundException.withId(
              WeatherErrorCode.TEMPERATURE_NOF_FOUND,
              weather.getTemperatureId()
          );
        });

    log.debug("날씨 요약 정보 조회 완료 - weatherId: {}", weather.getId());

    return weatherMapper.toWeatherSummaryDto(
        weather.getId(),
        weather.getSkyStatus(),
        precipitation,
        temperature
    );
  }
}
