package com.part4.team09.otboo.module.domain.weather.batch;

import com.part4.team09.otboo.module.domain.location.entity.Location;
import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherApiData;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse.Response.Body.Items.Item;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.external.WeatherApiClient;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

@RequiredArgsConstructor
public class WeatherReader implements ItemStreamReader<WeatherApiData> {

  private final ItemStreamReader<Location> locationReader;
  private final WeatherApiClient weatherApiClient;
  private final DongRepository dongRepository;
  private final WeatherRepository weatherRepository;
  private final WeatherCache weatherCache;

  private int x = 0;
  private int y = 0;
  private Location currentLocation;

  @Override
  public WeatherApiData read() throws Exception {
    while (true) {
      currentLocation = locationReader.read();
      if (currentLocation == null) {
        return null; // 모든 location 끝
      }

      dongRepository.findById(currentLocation.getDongId())
        .ifPresent(dong -> {
          x = dong.getX();
          y = dong.getY();
        });

      List<Weather> cachedWeathers = weatherCache.getData(x, y);
      if (cachedWeathers != null && !cachedWeathers.isEmpty()) {
        cachedWeathers.forEach(cachedWeather -> {
          weatherRepository.findByLocationIdAndForecastAt(currentLocation.getId(),
              cachedWeather.getForecastAt())
            .ifPresentOrElse(
              existing -> {
                existing.updateForecastedAt(cachedWeather.getForecastedAt());
                existing.updateForecastAt(cachedWeather.getForecastAt());
                existing.updateSkyStatus(cachedWeather.getSkyStatus());
                existing.updateLocationId(currentLocation.getId());
                existing.updatePrecipitationId(cachedWeather.getPrecipitationId());
                existing.updateHumidityId(cachedWeather.getHumidityId());
                existing.updateTemperatureId(cachedWeather.getTemperatureId());
                existing.updateWindSpeedId(cachedWeather.getWindSpeedId());
                weatherRepository.save(existing);
              },
              () -> {
                Weather newWeather = Weather.create(
                  cachedWeather.getForecastedAt(),
                  cachedWeather.getForecastAt(),
                  cachedWeather.getSkyStatus(),
                  currentLocation.getId(),
                  cachedWeather.getPrecipitationId(),
                  cachedWeather.getHumidityId(),
                  cachedWeather.getTemperatureId(),
                  cachedWeather.getWindSpeedId()
                );
                weatherRepository.save(newWeather);
              }
            );
        });

        // 👉 다음 Location 처리 위해 루프 계속
        continue;
      }

      // ❌ 캐시에 없으면 외부 API 호출해서 반환
      List<Item> items = weatherApiClient.getWeatherApiResponse(x, y);
      return new WeatherApiData(currentLocation.getId(), items, x, y);
    }
  }

//  @PostConstruct
//  private void initForecastAts() {
//    List<LocalDateTime> localDateTimes = new ArrayList<>();
//
//    for (int i = 0; i < 3; i++) {
//      if (i == 1) {
//        localDateTimes.add(LocalDate.now().minusDays(i).atTime(12, 0));
//      }
//      localDateTimes.add(LocalDate.now().plusDays(i).atTime(12, 0));
//    }
//
//    forecastAts = List.copyOf(localDateTimes);
//  }

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
