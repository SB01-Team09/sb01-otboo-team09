package com.part4.team09.otboo.module.domain.weather.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.domain.location.entity.Coordinate;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherApiData;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse.Response.Body.Items.Item;
import com.part4.team09.otboo.module.domain.weather.external.WeatherApiClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ItemStreamReader;

@ExtendWith(MockitoExtension.class)
class WeatherReaderTest {

  @Mock
  private ItemStreamReader<Coordinate> coordinateReader;

  @Mock
  private WeatherApiClient weatherApiClient;

  @InjectMocks
  private WeatherReader weatherReader;

  @Test
  void read_should_return_weatherApiData_when_coordinate_exists() throws Exception {
    // given
    int x = 60;
    int y = 127;
    Coordinate coordinate = Coordinate.create(x, y);

    Item item1 = mock(Item.class);
    Item item2 = mock(Item.class);
    List<Item> apiItems = List.of(item1, item2);

    when(coordinateReader.read()).thenReturn(coordinate);
    when(weatherApiClient.getWeatherApiResponse(x, y)).thenReturn(apiItems);

    // when
    WeatherApiData result = weatherReader.read();

    // then
    assertThat(result).isNotNull();
    assertThat(result.coordinate()).isEqualTo(coordinate);
    assertThat(result.items()).containsExactlyElementsOf(apiItems);
  }

  @Test
  void read_should_return_null_when_coordinateReader_returns_null() throws Exception {
    // given
    when(coordinateReader.read()).thenReturn(null);

    // when
    WeatherApiData result = weatherReader.read();

    // then
    assertThat(result).isNull();
  }

  @Test
  void read_should_throw_WeatherReadException_when_api_call_fails() throws Exception {
    // given
    int x = 60;
    int y = 127;
    Coordinate coordinate = Coordinate.create(x, y);

    when(coordinateReader.read()).thenReturn(coordinate);
    when(weatherApiClient.getWeatherApiResponse(x, y))
      .thenThrow(new RuntimeException("API failure"));

    // when / then
    org.junit.jupiter.api.Assertions.assertThrows(
      com.part4.team09.otboo.module.domain.weather.exception.WeatherReadException.class,
      () -> weatherReader.read()
    );
  }
}