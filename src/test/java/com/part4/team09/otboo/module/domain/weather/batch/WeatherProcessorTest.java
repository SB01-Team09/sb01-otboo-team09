package com.part4.team09.otboo.module.domain.weather.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = WeatherProcessor.class)
class WeatherProcessorTest {

  @Autowired
  private WeatherProcessor weatherProcessor;

  @Test
  void process_test() throws IOException {
    // given
    List<Item> items = getItems();
    String locationId = "1111111111";
    int x = 60;
    int y = 127;

    WeatherApiData weatherApiData = new WeatherApiData(locationId, items, x, y);

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