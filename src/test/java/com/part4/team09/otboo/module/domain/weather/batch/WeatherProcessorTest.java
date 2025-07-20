package com.part4.team09.otboo.module.domain.weather.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.domain.location.entity.Coordinate;
import com.part4.team09.otboo.module.domain.location.repository.CoordinateRepository;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherApiData;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherData;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse.Response.Body.Items.Item;
import com.part4.team09.otboo.module.domain.weather.entity.Humidity;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeatherProcessorTest {

  @Mock
  private CoordinateRepository coordinateRepository;

  @InjectMocks
  private WeatherProcessor weatherProcessor;

  @Test
  void process_test() throws IOException {
    // given
    List<Item> items = getItems();
    int x = 60;
    int y = 127;
    Coordinate coordinate = Coordinate.create(x, y);

    WeatherApiData weatherApiData = new WeatherApiData(items, coordinate);

    // when
    List<WeatherData> weatherDatas = weatherProcessor.process(weatherApiData);

    // then
    assertNotNull(weatherDatas);
    assertEquals(4, weatherDatas.size());

    for (int i = 0; i < weatherDatas.size(); i++) {
      Humidity humidity = weatherDatas.get(i).humidity();
      Temperature temperature = weatherDatas.get(i).temperature();
      if (i == 0) {
        assertNull(humidity.getComparedToDayBefore());
        assertNull(temperature.getComparedToDayBefore());
      } else {
        assertNotNull(humidity.getComparedToDayBefore());
        assertNotNull(temperature.getComparedToDayBefore());
      }
    }
  }

  private List<Item> getItems() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();

    InputStream inputStream = getClass().getClassLoader()
      .getResourceAsStream("weather-api-response.json");

    WeatherApiResponse weatherApiResponse = objectMapper.readValue(inputStream,
      WeatherApiResponse.class);

    return weatherApiResponse.response().body().items().item();
  }
}