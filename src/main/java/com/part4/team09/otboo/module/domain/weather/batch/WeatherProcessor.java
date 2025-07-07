package com.part4.team09.otboo.module.domain.weather.batch;

import com.part4.team09.otboo.module.domain.weather.dto.WeatherApiData;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherData;
import com.part4.team09.otboo.module.domain.weather.dto.response.WeatherApiResponse.Response.Body.Items.Item;
import com.part4.team09.otboo.module.domain.weather.entity.Humidity;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation.PrecipitationType;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed;
import com.part4.team09.otboo.module.domain.weather.entity.WindSpeed.AsWord;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherProcessor implements ItemProcessor<WeatherApiData, List<WeatherData>> {

  private static final String TIME_MORNING = "0600";
  private static final String TIME_AFTERNOON = "1500";
  private static final String TIME_NOON = "1200";
  private static final int CHUNK_SIZE = 290;

  @Override
  public List<WeatherData> process(WeatherApiData data) {
    List<Item> items = data.items();

    int currentIndex = 0;
    int endIndex = CHUNK_SIZE;

    List<WeatherData> weatherDatas = new ArrayList<>();
    while (currentIndex < items.size()) {
      WeatherExtractionContext context = new WeatherExtractionContext();
      for (int i = currentIndex; i < endIndex; i++) {
        extractForecastData(items.get(i), context);

        context.x = data.x();
        context.y = data.y();
      }
      weatherDatas.add(context.toWeatherData(data.locationId()));
      currentIndex = endIndex;
      endIndex = Math.min(currentIndex + CHUNK_SIZE, items.size());
    }

    for (int i = 0; i < weatherDatas.size(); i++) {
      if (i == 0) {
        continue;
      }

      Humidity currentHumidity = weatherDatas.get(i).humidity();
      Humidity beforeHumidity = weatherDatas.get(i - 1).humidity();

      double currentHumidityValue = currentHumidity.getCurrent();
      double beforeHumidityValue = beforeHumidity.getCurrent();

      currentHumidity.updateComparedToDayBefore(currentHumidityValue - beforeHumidityValue);

      Temperature currentTemperature = weatherDatas.get(i).temperature();
      Temperature beforeTemperature = weatherDatas.get(i - 1).temperature();

      double currentTemperatureValue = currentTemperature.getCurrent();
      double beforeTemperatureValue = beforeTemperature.getCurrent();

      currentTemperature.updateComparedToDayBefore(
        currentTemperatureValue - beforeTemperatureValue);
    }

    return weatherDatas;
  }

  private void extractForecastData(Item item, WeatherExtractionContext ctx) {
    String fcstTime = item.fcstTime();
    String category = item.category();
    String value = item.fcstValue();

    if (fcstTime.equals(TIME_MORNING) && category.equals("TMN")) {
      ctx.minTemperature = Double.parseDouble(value);
      return;
    }

    if (fcstTime.equals(TIME_AFTERNOON) && category.equals("TMX")) {
      ctx.maxTemperature = Double.parseDouble(value);
      return;
    }

    if (!fcstTime.equals(TIME_NOON)) {
      return;
    }

    switch (category) {
      case "TMP" -> ctx.currentTemperature = Double.parseDouble(value);
      case "WSD" -> {
        ctx.windSpeed = Double.parseDouble(value);
        ctx.windAsWord = AsWord.fromSpeed(ctx.windSpeed);
      }
      case "SKY" -> ctx.skyStatus = SkyStatus.of(Integer.parseInt(value));
      case "PTY" -> ctx.precipitationType = PrecipitationType.of(Integer.parseInt(value));
      case "POP" -> ctx.precipitationProbability = Double.parseDouble(value);
      case "PCP" -> {
        if (value.equals("1mm 미만")) {
          ctx.precipitationAmount = 0.5; // 임시 값
        } else if (value.equals("강수없음")) {
          ctx.precipitationAmount = 0.0;
        } else {
          Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*mm");
          Matcher matcher = pattern.matcher(value);
          if (matcher.find()) {
            ctx.precipitationAmount = Double.parseDouble(matcher.group(1));
          }
        }
      }
      case "REH" -> {
        ctx.currentHumidity = Double.parseDouble(value);
        String baseAt = item.baseDate() + item.baseTime();
        String fcstAt = item.fcstDate() + item.fcstTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        ctx.forecastedAt = LocalDateTime.parse(baseAt, formatter);
        ctx.forecastAt = LocalDateTime.parse(fcstAt, formatter);
      }
    }
  }

  private static class WeatherExtractionContext {

    double currentHumidity = 0.0;
    Double comparedToDayBeforeHumidity = null;

    PrecipitationType precipitationType = null;
    double precipitationAmount = 0.0;
    double precipitationProbability = 0.0;

    double currentTemperature = 0.0;
    Double comparedToDayBeforeTemperature = null;
    double minTemperature = 0.0;
    double maxTemperature = 0.0;

    double windSpeed = 0.0;
    AsWord windAsWord = null;

    LocalDateTime forecastAt = null;
    LocalDateTime forecastedAt = null;
    SkyStatus skyStatus = null;

    int x = 0;
    int y = 0;

    WeatherData toWeatherData(String locationId) {
      return new WeatherData(
        Humidity.create(currentHumidity, comparedToDayBeforeHumidity),
        Precipitation.create(precipitationType, precipitationAmount, precipitationProbability),
        Temperature.create(currentTemperature, comparedToDayBeforeTemperature, minTemperature,
          maxTemperature),
        WindSpeed.create(windSpeed, windAsWord),
        forecastedAt,
        forecastAt,
        skyStatus,
        locationId,
        x,
        y
      );
    }
  }
}

