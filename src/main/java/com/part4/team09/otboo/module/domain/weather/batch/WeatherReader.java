package com.part4.team09.otboo.module.domain.weather.batch;

import com.part4.team09.otboo.module.domain.location.entity.Coordinate;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherApiData;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse.Response.Body.Items.Item;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherReadException;
import com.part4.team09.otboo.module.domain.weather.external.WeatherApiClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

@RequiredArgsConstructor
public class WeatherReader implements ItemStreamReader<WeatherApiData> {

  private final ItemStreamReader<Coordinate> coordinateReader;
  private final WeatherApiClient weatherApiClient;

  @Override
  public WeatherApiData read() throws Exception {
    while (true) {
      Coordinate currentCoordinate = coordinateReader.read();
      if (currentCoordinate == null) {
        return null;
      }

      List<Item> items = fetchFromApi(currentCoordinate);
      return new WeatherApiData(items, currentCoordinate);
    }
  }

  private List<Item> fetchFromApi(Coordinate coordinate) {
    try {
      return weatherApiClient.getWeatherApiResponse(coordinate.getX(), coordinate.getY());
    } catch (Exception e) {
      throw WeatherReadException.withId(coordinate.getId());
    }
  }

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    coordinateReader.open(executionContext);
  }

  @Override
  public void update(ExecutionContext executionContext) throws ItemStreamException {
    coordinateReader.update(executionContext);
  }

  @Override
  public void close() throws ItemStreamException {
    coordinateReader.close();
  }
}
