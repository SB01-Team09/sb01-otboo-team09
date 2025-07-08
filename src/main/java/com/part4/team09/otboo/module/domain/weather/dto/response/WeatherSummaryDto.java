package com.part4.team09.otboo.module.domain.weather.dto.response;

import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import com.part4.team09.otboo.module.domain.weather.entity.Weather.SkyStatus;
import java.util.UUID;

public record WeatherSummaryDto(
    UUID weatherId,
    SkyStatus skyStatus,
    Precipitation precipitation,
    Temperature temperature
) {

}
