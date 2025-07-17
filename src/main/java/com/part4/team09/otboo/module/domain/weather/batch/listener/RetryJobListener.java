package com.part4.team09.otboo.module.domain.weather.batch.listener;

import com.part4.team09.otboo.module.domain.location.repository.LocationRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
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
  private final LocationRepository locationRepository;

  @Override
  public void afterJob(JobExecution jobExecution) {
    if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
      return;
    }

    String jobName = jobExecution.getJobInstance().getJobName();

    // 🔒 retryJob이 자기 자신을 반복 실행하지 않도록 방지
    if ("retryJob".equals(jobName)) {
      log.info("현재 실행된 Job이 retryJob이므로 다시 실행하지 않습니다.");
      return;
    }

    boolean hasRetryTargets = locationRepository.existsLocationNotInWeather(LocalDate.now().atStartOfDay());

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
