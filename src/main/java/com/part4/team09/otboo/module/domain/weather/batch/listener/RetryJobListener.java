package com.part4.team09.otboo.module.domain.weather.batch.listener;

import com.part4.team09.otboo.module.domain.location.repository.CoordinateRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryJobListener implements JobExecutionListener {

  private final JobLauncher jobLauncher;
  private final JobRegistry jobRegistry;
  private final CoordinateRepository coordinateRepository;

  @Override
  public void afterJob(JobExecution jobExecution) {
    boolean hasRetryTargets =
      coordinateRepository.existsCoordinateNotInWeather(LocalDate.now().atStartOfDay());

    if (!hasRetryTargets) {
      log.info("Retry 대상이 없으므로 retryJob은 실행하지 않습니다.");
      return;
    }

    try {
      Job retryJob = jobRegistry.getJob("retryJob");
      jobLauncher.run(
        retryJob,
        new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .toJobParameters()
      );
      log.info("RetryJob이 실행되었습니다.");
    } catch (Exception e) {
      log.error("RetryJob 실행 실패", e);
    }
  }
}
