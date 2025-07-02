package com.part4.team09.otboo.module.domain.weather.batch;

import com.part4.team09.otboo.module.domain.location.entity.Dong;
import com.part4.team09.otboo.module.domain.location.entity.Location;
import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherApiData;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse.Response.Body.Items.Item;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.external.WeatherApiClient;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

@RequiredArgsConstructor
public class WeatherReader implements ItemStreamReader<WeatherApiData> {

  private static final int CHUNK_SIZE = 290;

  private final ItemStreamReader<Location> locationReader;
  private final WeatherApiClient weatherApiClient;
  private final DongRepository dongRepository;
  private final WeatherRepository weatherRepository;
  private final WeatherCache weatherCache;

  private List<Item> currentApiDataBuffer = new ArrayList<>();
  private int currentIndex = 0;
  private int x = 0;
  private int y = 0;
  private Location currentLocation;

  @Override
  public WeatherApiData read() throws Exception {
    while (true) {
      // 아직 처리하지 않은 데이터가 있으면 chunk 로 반환
      if (hasRemainingChunk()) {
        return getNextChunk(x, y);
      }

      // 다음 Location 가져오기
      currentLocation = locationReader.read();
      if (currentLocation == null) {
        return null;
      }

      // 1. 좌표 조회
      Optional<Dong> optionalDong = dongRepository.findById(currentLocation.getDongId());
      if (optionalDong.isEmpty()) {
        continue; // Dong 못 찾으면 스킵
      }

      Dong dong = optionalDong.get();
      x = dong.getX();
      y = dong.getY();

      // 2. 캐시 조회
      Weather cachedWeather = weatherCache.getData(x, y, getDate());
      if (cachedWeather != null) {
        // ✅ 캐시에 있으면 저장하고 스킵
        Weather weather = Weather.create(
          cachedWeather.getForecastAt(),
          cachedWeather.getForecastedAt(),
          cachedWeather.getSkyStatus(),
          currentLocation.getId(),
          cachedWeather.getPrecipitationId(),
          cachedWeather.getHumidityId(),
          cachedWeather.getTemperatureId(),
          cachedWeather.getWindSpeedId()
        );

        weatherRepository.save(weather);
        continue;
      }

      currentApiDataBuffer = weatherApiClient.getWeatherApiResponse(dong.getX(), dong.getY());
      currentIndex = 0;

      // 받아온 데이터가 비어있다면 다음 Location 으로 넘어감
      if (!currentApiDataBuffer.isEmpty()) {
        return getNextChunk(x, y);
      }
    }
  }

  private boolean hasRemainingChunk() {
    return currentIndex < currentApiDataBuffer.size();
  }

  private WeatherApiData getNextChunk(int x, int y) {
    int endIndex = Math.min(currentIndex + CHUNK_SIZE, currentApiDataBuffer.size());
    List<Item> chunk = currentApiDataBuffer.subList(currentIndex, endIndex);
    currentIndex = endIndex;
    return new WeatherApiData(currentLocation.getId(), chunk, x, y);
  }

  private List<Item> fetchWeatherDataFor(Location location) {
    return dongRepository.findById(location.getDongId())
      .map(dong -> {
        x = dong.getX();
        y = dong.getY();
        return weatherApiClient.getWeatherApiResponse(dong.getX(), dong.getY());
      })
      .orElseGet(List::of); // 예외 대신 빈 리스트 반환
  }

  private LocalDateTime getDate() {
    if (currentIndex == 0) {
      return LocalDate.now().minusDays(1).atTime(12, 0);
    } else {
      return LocalDate.now().plusDays(currentIndex / 290 - 1).atTime(12, 0);
    }
  }
  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    locationReader.open(executionContext);
  }

  @Override
  public void update(ExecutionContext executionContext) throws ItemStreamException {
    locationReader.update(executionContext);
  }

  @Override
  public void close() throws ItemStreamException {
    locationReader.close();
  }
}
