package com.part4.team09.otboo.module.domain.weather.batch;

import com.part4.team09.otboo.module.domain.location.entity.Location;
import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherApiData;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse.Response.Body.Items.Item;
import com.part4.team09.otboo.module.domain.weather.external.WeatherApiClient;
import java.util.ArrayList;
import java.util.List;
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

  private List<Item> currentApiDataBuffer = new ArrayList<>();
  private int currentIndex = 0;
  private Location currentLocation;

  @Override
  public WeatherApiData read() throws Exception {
    while (true) {
      // 아직 처리하지 않은 데이터가 있으면 chunk 로 반환
      if (hasRemainingChunk()) {
        return getNextChunk();
      }

      // 다음 Location 가져오기
      currentLocation = locationReader.read();
      if (currentLocation == null) {
        return null;
      }

      currentApiDataBuffer = fetchWeatherDataFor(currentLocation);
      currentIndex = 0;

      // 받아온 데이터가 비어있다면 다음 Location 으로 넘어감
      if (!currentApiDataBuffer.isEmpty()) {
        return getNextChunk();
      }
    }
  }

  private boolean hasRemainingChunk() {
    return currentIndex < currentApiDataBuffer.size();
  }

  private WeatherApiData getNextChunk() {
    int endIndex = Math.min(currentIndex + CHUNK_SIZE, currentApiDataBuffer.size());
    List<Item> chunk = currentApiDataBuffer.subList(currentIndex, endIndex);
    currentIndex = endIndex;
    return new WeatherApiData(currentLocation.getId(), chunk);
  }

  private List<Item> fetchWeatherDataFor(Location location) {
    return dongRepository.findById(location.getDongId())
      .map(dong -> weatherApiClient.getWeatherApiResponse(dong.getX(), dong.getY()))
      .orElseGet(List::of); // 예외 대신 빈 리스트 반환
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
