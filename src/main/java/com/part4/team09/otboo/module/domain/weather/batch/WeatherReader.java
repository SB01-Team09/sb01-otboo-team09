package com.part4.team09.otboo.module.domain.weather.batch;

import com.part4.team09.otboo.module.domain.location.entity.Dong;
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

  private final ItemStreamReader<Location> locationReader;
  private final WeatherApiClient weatherApiClient;
  private final DongRepository dongRepository;
  private List<Item> currentApiDataBuffer = new ArrayList<>();
  private int currentIndex = 0;
  private Location currentLocation;

  @Override
  public WeatherApiData read() throws Exception {
    // 현재 버퍼에 처리할 데이터가 남아있으면 290개씩 잘라서 반환
    if (currentIndex < currentApiDataBuffer.size()) {
      int endIndex = Math.min(currentIndex + 290, currentApiDataBuffer.size());
      List<Item> subList = currentApiDataBuffer.subList(currentIndex, endIndex);
      currentIndex = endIndex;
      return new WeatherApiData(currentLocation.getId(), subList);
    }

    // 버퍼에 처리할 데이터가 없으면 다음 Location 읽고 API 호출해서 데이터 채움
    currentLocation = locationReader.read();
    if (currentLocation == null) {
      return null; // 더 이상 읽을 Location 없으면 종료
    }

    // 외부 API 호출하여 데이터 받아오기
    currentApiDataBuffer = callExternalApi(currentLocation);
    currentIndex = 0;

    // 받아온 데이터가 없으면 다음 Location 시도 (재귀 or 반복)
    if (currentApiDataBuffer.isEmpty()) {
      return read(); // 빈 데이터면 재귀 호출하여 다음 Location 처리
    }

    // 새로 받은 데이터 중 290개씩 첫 번째 chunk 반환
    int endIndex = Math.min(currentIndex + 290, currentApiDataBuffer.size());
    List<Item> subList = currentApiDataBuffer.subList(currentIndex, endIndex);
    currentIndex = endIndex;
    return new WeatherApiData(currentLocation.getId(), subList);
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

  private List<Item> callExternalApi(Location location) {
    Dong dong = dongRepository.findById(location.getDongId()).get();
    return weatherApiClient.getWeatherApiResponse(dong.getX(), dong.getY());
  }
}
