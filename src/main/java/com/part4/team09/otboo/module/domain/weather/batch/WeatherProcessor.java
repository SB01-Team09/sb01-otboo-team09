package com.part4.team09.otboo.module.domain.weather.batch;

import com.part4.team09.otboo.module.domain.weather.dto.WeatherApiData;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherData;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse.Response.Body.Items.Item;
import com.part4.team09.otboo.module.domain.weather.entity.Humidity;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation.PrecipitationType;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed.AsWord;
import com.part4.team09.otboo.module.domain.weather.repository.HumidityRepository;
import com.part4.team09.otboo.module.domain.weather.repository.TemperatureRepository;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherProcessor implements ItemProcessor<WeatherApiData, WeatherData> {

  private final TemperatureRepository temperatureRepository;
  private final HumidityRepository humidityRepository;
  private final WeatherRepository weatherRepository;
  private boolean isYesterday = false;
  private String currentLocationId = null;

  @Override
  public WeatherData process(WeatherApiData data) throws Exception {
    String locationId = data.locationId();

    if (!locationId.equals(currentLocationId)) {
      isYesterday = false;
      currentLocationId = locationId;
    }

    List<Item> items = data.items();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    double currentHumidity = 0.0; //
    Double comparedToDayBeforeHumidity = null; //

    PrecipitationType precipitationType = null; //
    double amount = 0.0; //
    double probability = 0.0; //

    double currentTemperature = 0.0; //
    Double comparedToDayBeforeTemperature = null;
    double minTemperature = 0.0; //
    double maxTemperature = 0.0; //

    double speed = 0.0; //
    AsWord asWord = null; //

    LocalDateTime forecastAt = null; //
    LocalDateTime forecastedAt = null; //
    SkyStatus skyStatus = null; //

    for (Item item : items) {
      String fcstTime = item.fcstTime();
      String fcstDate = item.fcstDate();
      String category = item.category();

      if (fcstTime.equals("0600")) {
        if (category.equals("TMN")) {
          minTemperature = Double.parseDouble(item.fcstValue());
          String baseDate = item.baseDate();
          String date = getYesterDayDate();
          if (fcstDate.equals(date)) {
            isYesterday = true;
          }
        }
        continue;
      }

      if (fcstTime.equals("1500")) {
        if (category.equals("TMX")) {
          maxTemperature = Double.parseDouble(item.fcstValue());
        }
        continue;
      }

      if (fcstTime.equals("1200")) {
        if (category.equals("TMP")) {
          currentTemperature = Double.parseDouble(item.fcstValue());
          continue;
        }
        if (category.equals("WSD")) {
          speed = Double.parseDouble(item.fcstValue());
          asWord = AsWord.fromSpeed(speed);
          continue;
        }
        if (category.equals("SKY")) {
          skyStatus = SkyStatus.of(Integer.parseInt(item.fcstValue()));
          continue;
        }
        if (category.equals("PTY")) {
          precipitationType = PrecipitationType.of(Integer.parseInt(item.fcstValue()));
          continue;
        }
        if (category.equals("POP")) {
          probability = Double.parseDouble(item.fcstValue());
          continue;
        }
        if (category.equals("PCP")) {
          if (item.fcstValue().equals("강수없음")) {
            continue;
          } else {
            amount = Double.parseDouble(item.fcstValue());
          }
          continue;
        }
        if (category.equals("REH")) {
          currentHumidity = Double.parseDouble(item.fcstValue());

          String baseAt = item.baseDate() + item.baseTime();
          String fcstAt = item.fcstDate() + item.fcstTime();

          forecastAt = LocalDateTime.parse(baseAt, formatter);
          forecastedAt = LocalDateTime.parse(fcstAt, formatter);
        }
      }
    }

    if (isYesterday) {
      LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
      LocalDateTime end = LocalDate.now().atStartOfDay();
      Weather weather = weatherRepository
        .findFirstByLocationIdAndForecastAtBetween(locationId, start, end).orElse(null);

      if (weather != null) {
        Temperature findedTemperature =
          temperatureRepository.findById(weather.getTemperatureId()).get();
        double yesterdayTemperature = findedTemperature.getCurrent();
        comparedToDayBeforeTemperature = currentTemperature - yesterdayTemperature;

        Humidity findedHumidity = humidityRepository.findById(weather.getHumidityId()).get();
        double yesterdayHumidity = findedHumidity.getCurrent();
        comparedToDayBeforeHumidity = currentHumidity - yesterdayHumidity;
      }
    }

    Humidity humidity = Humidity.create(currentHumidity, comparedToDayBeforeHumidity);
    Precipitation precipitation = Precipitation.create(precipitationType, amount, probability);
    Temperature temperature = Temperature.create(currentTemperature, comparedToDayBeforeTemperature,
      minTemperature, maxTemperature);
    WindSpeed windSpeed = WindSpeed.create(speed, asWord);
    WeatherData weatherData = new WeatherData(
      humidity,
      precipitation,
      temperature,
      windSpeed,
      forecastAt,
      forecastedAt,
      skyStatus,
      locationId
    );
    return weatherData;
  }

  private String getYesterDayDate() {
    return LocalDate.now()
      .minusDays(1)
      .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
  }
}
