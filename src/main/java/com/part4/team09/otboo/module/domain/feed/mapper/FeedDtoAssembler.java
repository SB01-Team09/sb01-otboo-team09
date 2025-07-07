package com.part4.team09.otboo.module.domain.feed.mapper;

import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.exception.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.feed.service.LikeService;
import com.part4.team09.otboo.module.domain.feed.service.OotdService;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherErrorCode;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherNotFoundException;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedDtoAssembler {

  private final FeedMapper feedMapper;

  private final OotdService ootdService;
  private final LikeService likeService;

  private final FeedRepository feedRepository;
  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;

  public FeedDto assemble(UUID feedId, UUID userId) {
    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> FeedNotFoundException.withId(feedId));

    return assemble(feed, userId);
  }

  public FeedDto assemble(Feed feed, UUID userId) {
    User author = userRepository.findById(feed.getAuthorId())
        .orElseThrow(() -> UserNotFoundException.withId(feed.getAuthorId()));

    Weather weather = weatherRepository.findById(feed.getWeatherId())
        .orElseThrow(() -> WeatherNotFoundException.withId(WeatherErrorCode.WEATHER_NOF_FOUND, feed.getWeatherId()));

    List<OotdDto> ootds = ootdService.getOotds(feed.getId());
    boolean likedByMe = likeService.isLikedByMe(userId, feed.getId());

    return feedMapper.toDto(feed, author, weather, ootds, likedByMe);
  }
}
