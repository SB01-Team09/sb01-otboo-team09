package com.part4.team09.otboo.module.domain.feed.repository;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedListRequest;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.entity.Ootd;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.repository.PrecipitationRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class FeedRepositoryQueryDSLTest {

    @Autowired
    private FeedRepositoryQueryDSL feedRepositoryQueryDSL;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeatherRepository weatherRepository;

    @Autowired
    private PrecipitationRepository precipitationRepository;

    @Autowired
    private OotdRepository ootdRepository;

    @Test
    void getFeeds_성공() {
        // given
        User author = User.createUser("연경@email.com", "연경", "otboo1!");
        userRepository.save(author);

        Precipitation precipitation = Precipitation.create(
                Precipitation.PrecipitationType.RAIN,
                10.5,
                80.0
        );
        precipitationRepository.save(precipitation);

        Weather weather = Weather.create(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now(),
                Weather.SkyStatus.CLOUDY,
                "location-1",
                precipitation.getId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
        weatherRepository.save(weather);

        Feed feed = Feed.create(author.getId(), weather.getId(), "OOTD입니다");
        feedRepository.save(feed);

        Ootd ootd = Ootd.create(feed.getId(), UUID.randomUUID());
        ootdRepository.save(ootd);

        FeedListRequest request = new FeedListRequest(
                null,
                null,
                10,
                "createdAt",
                SortDirection.DESCENDING,
                "OOTD",
                Weather.SkyStatus.CLOUDY,
                Precipitation.PrecipitationType.RAIN,
                author.getId()
        );

        // when
        List<Feed> result = feedRepositoryQueryDSL.getFeeds(request);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).contains("OOTD");
        assertThat(result.get(0).getAuthorId()).isEqualTo(author.getId());
    }

    @Test
    void countFeeds_성공() {
        // given
        User author = User.createUser("연경@email.com", "연경", "otboo1!");
        userRepository.save(author);

        Precipitation precipitation = Precipitation.create(
                Precipitation.PrecipitationType.SNOW,
                3.2,
                60.0
        );
        precipitationRepository.save(precipitation);

        Weather weather = Weather.create(
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now(),
                Weather.SkyStatus.CLEAR,
                "location-2",
                precipitation.getId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
        weatherRepository.save(weather);

        Feed feed = Feed.create(author.getId(), weather.getId(), "이건 OOTD 아님");
        feedRepository.save(feed);

        Ootd ootd = Ootd.create(feed.getId(), UUID.randomUUID());
        ootdRepository.save(ootd);

        FeedListRequest request = new FeedListRequest(
                null,
                null,
                10,
                "createdAt",
                SortDirection.DESCENDING,
                "이건",
                Weather.SkyStatus.CLEAR,
                Precipitation.PrecipitationType.SNOW,
                author.getId()
        );

        // when
        int count = feedRepositoryQueryDSL.countFeeds(request);

        // then
        assertThat(count).isEqualTo(1);
    }
}
