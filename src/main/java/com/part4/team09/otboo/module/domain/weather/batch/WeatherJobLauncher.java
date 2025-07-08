package com.part4.team09.otboo.module.domain.weather.batch;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherJobLauncher implements CommandLineRunner {

  private final JobLauncher jobLauncher;

  @Qualifier("weatherJob")
  private final Job weatherJob;

  @Override
  public void run(String... args) throws Exception {
//    init();
    // 추후 스케줄러로 전환할 예정
  }

  private void init()
    throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
    JobParameters params = new JobParametersBuilder()
      .addLong("run.id", System.currentTimeMillis()) // 항상 다른 파라미터로 실행
      .addString("unique", UUID.randomUUID().toString())  // 항상 새로운 파라미터
      .toJobParameters();

    jobLauncher.run(weatherJob, params);
  }
}
