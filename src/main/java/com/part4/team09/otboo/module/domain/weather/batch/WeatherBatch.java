package com.part4.team09.otboo.module.domain.weather.batch;

import com.part4.team09.otboo.module.common.monitoring.BatchMonitoringListener;
import com.part4.team09.otboo.module.domain.location.entity.Location;
import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherApiData;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherData;
import com.part4.team09.otboo.module.domain.weather.external.WeatherApiClient;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeatherBatch {

  private final EntityManagerFactory entityManagerFactory;
  private final WeatherProcessor weatherProcessor;
  private final WeatherWriter weatherWriter;
  private final WeatherApiClient weatherApiClient;
  private final DongRepository dongRepository;
  private final WeatherRepository weatherRepository;
  private final WeatherCache weatherCache;

  @Bean
  public Step weatherStep(JobRepository jobRepository,
    PlatformTransactionManager transactionManager) {
    return new StepBuilder("weatherStep", jobRepository)
      .<WeatherApiData, WeatherData>chunk(1, transactionManager)
      .reader(weatherReader())
      .processor(weatherProcessor)
      .writer(weatherWriter)
      .faultTolerant()
      .retryLimit(3) // 최대 3번 재시도
      .retry(Exception.class)
      .skip(Exception.class)
      .skipLimit(50) // 유연한 실패 허용
      .build();
  }

  @Bean
  public WeatherReader weatherReader() {
    return new WeatherReader(locationReader(), weatherApiClient, dongRepository, weatherRepository,
      weatherCache);
  }

  @Bean
  public JpaPagingItemReader<Location> locationReader() {
    JpaPagingItemReader<Location> reader = new JpaPagingItemReader<>();
    reader.setEntityManagerFactory(entityManagerFactory);
    reader.setQueryString("SELECT l FROM Location l");
    reader.setPageSize(10);
    return reader;
  }

  @Bean("weatherJob")
  public Job weatherJob(JobRepository jobRepository, Step weatherStep,
    MeterRegistry meterRegistry) {
    return new JobBuilder("weatherJob", jobRepository)
      .listener(new BatchMonitoringListener(meterRegistry))
      .start(weatherStep)
      .build();
  }
}
