package com.part4.team09.otboo.module.common.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchMonitoringListener implements JobExecutionListener {

  private final MeterRegistry meterRegistry;

  @Override
  public void beforeJob(JobExecution jobExecution) {
    jobExecution.getExecutionContext().putLong("startTime", System.currentTimeMillis());
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    long start = jobExecution.getExecutionContext().getLong("startTime");
    long duration = System.currentTimeMillis() - start;

    meterRegistry.timer("batch_job_duration", "job", jobExecution.getJobInstance().getJobName())
      .record(duration, TimeUnit.MILLISECONDS);
  }
}

