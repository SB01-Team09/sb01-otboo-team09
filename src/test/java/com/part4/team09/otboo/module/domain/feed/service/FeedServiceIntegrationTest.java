package com.part4.team09.otboo.module.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedListRequest;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.entity.Ootd;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.feed.repository.OotdRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.weather.entity.Humidity;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed;
import com.part4.team09.otboo.module.domain.weather.repository.HumidityRepository;
import com.part4.team09.otboo.module.domain.weather.repository.PrecipitationRepository;
import com.part4.team09.otboo.module.domain.weather.repository.TemperatureRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WindSpeedRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Disabled
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class FeedServiceIntegrationTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    FeedService feedService;
    @Autowired
    FeedRepository feedRepository;
    @Autowired
    WeatherRepository weatherRepository;
    @Autowired
    TemperatureRepository temperatureRepository;
    @Autowired
    HumidityRepository humidityRepository;
    @Autowired
    WindSpeedRepository windSpeedRepository;
    @Autowired
    PrecipitationRepository precipitationRepository;
    @Autowired
    OotdRepository ootdRepository;
    @Autowired
    ClothesRepository clothesRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("피드 목록 조회 통합 테스트 - 기본 검색 필터링 및 정렬 확인")
    void getFeedsSuccess() {
        // given
        User user = User.createUser("emai1@email.com", "연경", "otboo1!");
        userRepository.save(user);

        // 날씨 생성
        // 강수량
        Precipitation precipitation = Precipitation.create(
                Precipitation.PrecipitationType.RAIN,
                12.5,
                0.8
        );
        precipitationRepository.save(precipitation);

        // 온도
        Temperature temperature = Temperature.create(21.5, 1.3, 2, 5);
        temperatureRepository.save(temperature);

        // 습도
        Humidity humidity = Humidity.create(75.0, 10.3);
        humidityRepository.save(humidity);

        // 풍속
        WindSpeed windSpeed = WindSpeed.create(3.4, WindSpeed.AsWord.MODERATE);
        windSpeedRepository.save(windSpeed);

        Weather weather = Weather.create(
                LocalDateTime.now().plusHours(3),
                LocalDateTime.now(),
                Weather.SkyStatus.CLOUDY,
          UUID.randomUUID(),
                precipitation.getId(),
                humidity.getId(),
                temperature.getId(), // temperatureId
                windSpeed.getId()  // windSpeedId
        );
        weatherRepository.save(weather);

        // 피드 생성
        Feed feed = Feed.create(
                user.getId(),
                weather.getId(),
                "오늘 날씨와 어울리는 코디"
        );
        feedRepository.save(feed);

        // Clothes 생성
        Clothes clothes = Clothes.create(
                user.getId(),
                "그냥 옷",
                Clothes.ClothesType.OUTER,
                null
        );
        clothesRepository.save(clothes);

        // OOTD 생성
        Ootd ootd = Ootd.create(
                feed.getId(),
                clothes.getId()
        );
        ootdRepository.save(ootd);

        em.flush();
        em.clear();

        // when
        FeedListRequest request = new FeedListRequest(
                null, // cursor
                null, // idAfter
                10,   // limit
                "createdAt",
                SortDirection.DESCENDING,
                null, // keyword
                Weather.SkyStatus.CLOUDY,
                Precipitation.PrecipitationType.RAIN,
                user.getId()
        );

        FeedDtoCursorResponse result = feedService.getFeeds(user.getId(), request);

        // then
        assertThat(result.data()).hasSize(1);

        FeedDto dto = result.data().get(0);
        assertThat(dto.ootds()).anyMatch(o -> o.clothesId().equals(clothes.getId()));
        assertThat(dto.content()).isEqualTo("오늘 날씨와 어울리는 코디");
        assertThat(dto.author().userId()).isEqualTo(user.getId());
        assertThat(dto.weather().skyStatus()).isEqualTo(Weather.SkyStatus.CLOUDY);
        assertThat(dto.weather().precipitation().getType()).isEqualTo(Precipitation.PrecipitationType.RAIN);

        assertThat(result.hasNext()).isFalse();
    }

}
