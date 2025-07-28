package com.part4.team09.otboo.module.domain.weather.batch.retry;

import com.part4.team09.otboo.module.common.monitoring.BatchMonitoringListener;
import com.part4.team09.otboo.module.domain.weather.batch.WeatherProcessor;
import com.part4.team09.otboo.module.domain.weather.batch.WeatherReader;
import com.part4.team09.otboo.module.domain.weather.batch.WeatherWriter;
import com.part4.team09.otboo.module.domain.weather.batch.listener.RetryJobListener;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherApiData;
import com.part4.team09.otboo.module.domain.weather.dto.WeatherData;
import com.part4.team09.otboo.module.domain.weather.exception.WeatherReadException;
import com.part4.team09.otboo.module.domain.weather.external.WeatherApiClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class RetryBatch {

  private final WeatherProcessor weatherProcessor;
  private final WeatherWriter weatherWriter;
  private final WeatherApiClient weatherApiClient;
  private final FailedLocationReader failedLocationReader;
  private final BatchMonitoringListener batchMonitoringListener;
  private final RetryJobListener retryJobListener;

  @Bean
  public Step retryStep(JobRepository jobRepository,
    PlatformTransactionManager transactionManager) {
    return new StepBuilder("retryStep", jobRepository)
      .<WeatherApiData, List<WeatherData>>chunk(1, transactionManager)
      .reader(retryWeatherReader())
      .processor(weatherProcessor)
      .writer(weatherWriter)
      .faultTolerant()
      .retryLimit(3) // 최대 3번 재시도
      .retry(WeatherReadException.class)
      .skip(WeatherReadException.class)
      .skipLimit(50) // 유연한 실패 허용
      .build();
  }

  @Bean
  public WeatherReader retryWeatherReader() {
    return new WeatherReader(failedLocationReader, weatherApiClient);
  }

  @Bean("retryJob")
  public Job retryJob(JobRepository jobRepository, Step retryStep) {
    return new JobBuilder("retryJob", jobRepository)
      .start(retryStep)
      .listener(batchMonitoringListener)
      .listener(retryJobListener)
      .build();
  }
}
