package com.part4.team09.otboo.module.domain.weather.batch;

import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherCache {

  private final ConcurrentHashMap<String, List<Weather>> weatherCache = new ConcurrentHashMap<>();

  private final MeterRegistry meterRegistry;

  public void putData(int x, int y, List<Weather> weather) {
    String coordinate = x + "_" + y;
    weatherCache.putIfAbsent(coordinate, weather);
  }

  public List<Weather> getData(int x, int y) {
    meterRegistry
      .counter("method_calls", "method", "weatherCache.getData")
      .increment();

    String coordinate = x + "_" + y;
    List<Weather> weathers = weatherCache.get(coordinate);

    if (weathers != null) {
      meterRegistry
        .counter("cache_hit", "cache", "weatherCache.getData")
        .increment();
    }

    return weathers;
  }

  public boolean isExist(int x, int y) {
    String coordinate = x + "_" + y;
    return weatherCache.containsKey(coordinate);
  }
}
