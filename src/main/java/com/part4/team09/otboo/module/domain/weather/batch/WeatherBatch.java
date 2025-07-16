package com.part4.team09.otboo.module.domain.weather.batch;

import com.part4.team09.otboo.module.common.monitoring.BatchMonitoringListener;
import com.part4.team09.otboo.module.domain.location.entity.Coordinate;
import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.weather.batch.listener.RetryJobListener;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherApiData;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherData;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherReadException;
import com.part4.team09.otboo.module.domain.weather.external.WeatherApiClient;
import com.part4.team09.otboo.module.domain.weather.repository.WeatherRepository;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
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
  private final BatchMonitoringListener batchMonitoringListener;
  private final RetryJobListener retryJobListener;

  @Bean
  public Step weatherStep(JobRepository jobRepository,
    PlatformTransactionManager transactionManager) {
    return new StepBuilder("weatherStep", jobRepository)
      .<WeatherApiData, List<WeatherData>>chunk(1, transactionManager)
      .reader(weatherReader())
      .processor(weatherProcessor)
      .writer(weatherWriter)
      .faultTolerant()
      .retry(WeatherReadException.class)
      .retryLimit(3) // 최대 3번 재시도
      .skip(WeatherReadException.class)
      .skipLimit(50)
      .build();
  }

  @Bean
  public WeatherReader weatherReader() {
    return new WeatherReader(coordinateReader(), weatherApiClient);
  }

  @Bean
  public JpaPagingItemReader<Coordinate> coordinateReader() {
    JpaPagingItemReader<Coordinate> reader = new JpaPagingItemReader<>();
    reader.setEntityManagerFactory(entityManagerFactory);
    reader.setQueryString("SELECT c FROM Coordinate c");
    reader.setPageSize(50);
    reader.setSaveState(false);
    return reader;
  }

  @Bean("weatherJob")
  public Job weatherJob(JobRepository jobRepository, Step weatherStep) {
    return new JobBuilder("weatherJob", jobRepository)
      .start(weatherStep)
      .listener(batchMonitoringListener)
      .listener(retryJobListener)
      .build();
  }
}
