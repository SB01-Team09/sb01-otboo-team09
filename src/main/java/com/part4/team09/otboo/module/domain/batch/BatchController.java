package com.part4.team09.otboo.module.domain.batch;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/batch")
public class BatchController {

  private final JobLauncher jobLauncher;

  @Qualifier("locationJob")
  private final Job locationImportJob;

  @Qualifier("weatherJob")
  private final Job weatherJob;

  @GetMapping("/location")
  public void executeLocationJob()
    throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
    JobParameters params = new JobParametersBuilder()
      .addLong("run.id", System.currentTimeMillis()) // 항상 다른 파라미터로 실행
      .addString("unique", UUID.randomUUID().toString())  // 항상 새로운 파라미터
      .toJobParameters();

    jobLauncher.run(locationImportJob, params);
  }

  @GetMapping("/weather")
  public void executeWeatherJob()
    throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
    JobParameters params = new JobParametersBuilder()
      .addLong("run.id", System.currentTimeMillis()) // 항상 다른 파라미터로 실행
      .addString("unique", UUID.randomUUID().toString())  // 항상 새로운 파라미터
      .toJobParameters();

    jobLauncher.run(weatherJob, params);
  }

}
