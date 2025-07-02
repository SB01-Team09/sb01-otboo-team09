package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.feed.dto.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.entity.Ootd;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedMapper;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

  private final FeedRepository feedRepository;
  private final FeedMapper feedMapper;

  private final OotdService ootdService;

  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;

  // TODO: 로그인 한 사용자와 같은지 확인
  @Transactional
  public FeedDto create(FeedCreateRequest request) {
    User author = getUserOrThrow(request.authorId());
    Weather weather = getWeatherOrThrow(request.weatherId());

    Feed feed = Feed.create(request.authorId(), request.weatherId(), request.content());
    Feed savedFeed = feedRepository.save(feed);

    List<OotdDto> ootdDtos = ootdService.create(savedFeed.getId(), request.clothesIds());

    return feedMapper.toDto(savedFeed, author, weather, ootdDtos);
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
  }

  // TODO: 날씨 커스텀 예외로 변경
  private Weather getWeatherOrThrow(UUID weatherId) {
    return weatherRepository.findById(weatherId)
        .orElseThrow(() -> new EntityNotFoundException("Weather not found with id: " + weatherId));
  }
}
