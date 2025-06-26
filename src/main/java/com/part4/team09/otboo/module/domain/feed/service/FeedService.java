package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.feed.dto.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedMapper;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import jakarta.persistence.EntityNotFoundException;
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

  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;

  // TODO: 로그인 한 사용자와 같은지 확인
  @Transactional
  public FeedDto create(FeedCreateRequest request) {
    User author = getUserOrThrow(request.authorId());
    Weather weather = getWeatherOrThrow(request.weatherId());

    Feed feed = Feed.create(request.authorId(), request.weatherId(), request.content());
    feedRepository.save(feed);

    // TODO: ootd list 생성하고 저장해서 mapper에 같이 넘기기

    return feedMapper.toDto(feed, author, weather);
  }

  // TODO: 유저 커스텀 예외로 변경
  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
  }

  // TODO: 날시 커스텀 예외로 변경
  private Weather getWeatherOrThrow(UUID weatherId) {
    return weatherRepository.findById(weatherId)
        .orElseThrow(() -> new EntityNotFoundException("Weather not found with id: " + weatherId));
  }
}
