package com.part4.team09.otboo.module.domain.weather.batch;

import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherCache {

  private final ConcurrentHashMap<String, ConcurrentHashMap<LocalDateTime, Weather>> weatherCache =
    new ConcurrentHashMap<>(new ConcurrentHashMap<>());

  private final MeterRegistry meterRegistry;

  public void putData(int x, int y, LocalDateTime forecastAt, Weather weather) {
    String coordinate = x + "_" + y;

    if (weatherCache.get(coordinate) == null) {
      weatherCache.put(coordinate, new ConcurrentHashMap<>());
    }
    weatherCache.get(coordinate).put(forecastAt, weather);
  }

  public Weather getData(int x, int y, LocalDateTime forecastAt) {
    meterRegistry
      .counter("method_calls", "method", "weatherCache.getData")
      .increment();

    String coordinate = x + "_" + y;
    ConcurrentHashMap<LocalDateTime, Weather> map = weatherCache.get(coordinate);

    if (map != null) {
      meterRegistry
        .counter("cache_hit", "cache", "weatherCache.getData")
        .increment();

      return map.get(forecastAt);
    }

    return null;
  }

  public boolean isExist(int x, int y) {
    String coordinate = x + "_" + y;
    return weatherCache.containsKey(coordinate);
  }
}
