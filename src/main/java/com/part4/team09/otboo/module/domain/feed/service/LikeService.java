package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.entity.Like;
import com.part4.team09.otboo.module.domain.feed.exception.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedMapper;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.feed.repository.LikeRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

  private final LikeRepository likeRepository;

  private final FeedMapper feedMapper;

  private final FeedRepository feedRepository;
  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;

  @Transactional
  public FeedDto create(UUID userId, UUID feedId) {
    Feed feed = getFeedOrThrow(feedId);
    User user = getUserOrThrow(userId);
    Weather weather = getWeatherOrThrow(feed.getWeatherId());

    Like like = Like.create(feedId, userId);
    likeRepository.save(like);

    return feedMapper.toDto(feed, user, weather, List.of(), true);
  }

  public boolean isLikedByMe(UUID userId, UUID feedId) {
    return likeRepository.existsByUserIdAndFeedId(userId, feedId);
  }
  
  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
  }

  private Feed getFeedOrThrow(UUID feedId) {
    return feedRepository.findById(feedId)
        .orElseThrow(() -> FeedNotFoundException.withId(feedId));
  }

  // TODO: 날씨 커스텀 예외로 변경
  private Weather getWeatherOrThrow(UUID weatherId) {
    return weatherRepository.findById(weatherId)
        .orElseThrow(() -> new EntityNotFoundException("Weather not found with id: " + weatherId));
  }
}
