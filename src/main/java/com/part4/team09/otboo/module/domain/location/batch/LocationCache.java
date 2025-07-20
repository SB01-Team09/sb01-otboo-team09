package com.part4.team09.otboo.module.domain.location.batch;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationCache {

  // x_y : coordinateId
  private final ConcurrentHashMap<String, UUID> weatherCache = new ConcurrentHashMap<>();

  private final MeterRegistry meterRegistry;

  public void putData(int x, int y, UUID coordinateId) {
    String coordinate = x + "_" + y;
    weatherCache.putIfAbsent(coordinate, coordinateId);
  }

  public UUID getData(int x, int y) {
    meterRegistry
      .counter("method_calls", "method", "locationCache.getData")
      .increment();

    String coordinate = x + "_" + y;
    UUID coordinateId = weatherCache.get(coordinate);

    if (coordinateId != null) {
      meterRegistry
        .counter("cache_hit", "cache", "locationCache.getData")
        .increment();
    }

    return coordinateId;
  }
}
