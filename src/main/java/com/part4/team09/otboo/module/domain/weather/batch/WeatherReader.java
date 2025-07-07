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
        return null;
      }

      setCoordinates(currentLocation);

      if (processCachedDataIfExist()) {
        continue; // 다음 Location 으로
      }

      List<Item> items = fetchFromApi(x, y);
      return new WeatherApiData(currentLocation.getId(), items, x, y);
    }
  }

  private void setCoordinates(Location location) {
    dongRepository.findById(location.getDongId())
      .ifPresent(dong -> {
        x = dong.getX();
        y = dong.getY();
      });
  }

  private boolean processCachedDataIfExist() {
    List<Weather> cachedWeathers = weatherCache.getData(x, y);
    if (cachedWeathers == null || cachedWeathers.isEmpty()) {
      return false;
    }

    cachedWeathers.forEach(cached -> {
      weatherRepository.findByLocationIdAndForecastAt(currentLocation.getId(),
          cached.getForecastAt())
        .ifPresentOrElse(
          existing -> updateWeather(existing, cached),
          () -> saveNewWeather(cached)
        );
    });
    return true;
  }

  private void updateWeather(Weather existing, Weather cached) {
    existing.updateForecastedAt(cached.getForecastedAt());
    existing.updateForecastAt(cached.getForecastAt());
    existing.updateSkyStatus(cached.getSkyStatus());
    existing.updateLocationId(currentLocation.getId());
    existing.updatePrecipitationId(cached.getPrecipitationId());
    existing.updateHumidityId(cached.getHumidityId());
    existing.updateTemperatureId(cached.getTemperatureId());
    existing.updateWindSpeedId(cached.getWindSpeedId());
    weatherRepository.save(existing);
  }

  private void saveNewWeather(Weather cached) {
    Weather newWeather = Weather.create(
      cached.getForecastedAt(),
      cached.getForecastAt(),
      cached.getSkyStatus(),
      currentLocation.getId(),
      cached.getPrecipitationId(),
      cached.getHumidityId(),
      cached.getTemperatureId(),
      cached.getWindSpeedId()
    );
    weatherRepository.save(newWeather);
  }

  private List<Item> fetchFromApi(int x, int y) {
    return weatherApiClient.getWeatherApiResponse(x, y);
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
