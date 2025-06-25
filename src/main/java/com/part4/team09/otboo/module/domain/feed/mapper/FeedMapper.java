package com.part4.team09.otboo.module.domain.feed.mapper;

import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FeedMapper {

  public FeedDto toDto(Feed feed) {
    return  new FeedDto(
        UUID.randomUUID(),
        LocalDateTime.now(),
        LocalDateTime.now(),
        User.createUser("email", "name", "password"),
        Weather.create(
            LocalDateTime.now(),
            LocalDateTime.now(),
            SkyStatus.CLEAR,
            null,
            null,
            null,
            null,
            null
        ),
        List.of(),
        "content",
        0,
        0,
        false
    );
  }

}
