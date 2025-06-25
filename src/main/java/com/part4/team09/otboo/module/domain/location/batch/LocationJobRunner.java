package com.part4.team09.otboo.module.domain.location.batch;

import java.util.UUID;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LocationJobRunner implements CommandLineRunner {

  private final JobLauncher jobLauncher;
  private final Job locationImportJob;

  public LocationJobRunner(JobLauncher jobLauncher, Job locationImportJob) {
    this.jobLauncher = jobLauncher;
    this.locationImportJob = locationImportJob;
  }

  @Override
  public void run(String... args) throws Exception {
    JobParameters params = new JobParametersBuilder()
      .addLong("run.id", System.currentTimeMillis()) // 항상 다른 파라미터로 실행
      .addString("unique", UUID.randomUUID().toString())  // 항상 새로운 파라미터
      .toJobParameters();

    jobLauncher.run(locationImportJob, params);
  }
}
