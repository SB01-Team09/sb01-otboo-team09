package com.part4.team09.otboo.module.domain.location.batch;

import com.part4.team09.otboo.module.domain.location.repository.LocationRepository;
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

import java.util.UUID;

@Component
public class LocationJobRunner implements CommandLineRunner {

  private final JobLauncher jobLauncher;
  private final Job locationImportJob;
  private final LocationRepository locationRepository;

  public LocationJobRunner(JobLauncher jobLauncher,
    @Qualifier("locationJob") Job locationImportJob, LocationRepository locationRepository) {
    this.jobLauncher = jobLauncher;
    this.locationImportJob = locationImportJob;
    this.locationRepository = locationRepository;
  }

  @Override
  public void run(String... args) throws Exception {
//    init();
  }

  private void init()
    throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
    if (locationRepository.count() > 0) {
      return; // 이미 데이터가 존재하면 배치 실행하지 않음
    }

    JobParameters params = new JobParametersBuilder()
      .addLong("run.id", System.currentTimeMillis()) // 항상 다른 파라미터로 실행
      .addString("unique", UUID.randomUUID().toString())  // 항상 새로운 파라미터
      .toJobParameters();

    jobLauncher.run(locationImportJob, params);
  }
}
