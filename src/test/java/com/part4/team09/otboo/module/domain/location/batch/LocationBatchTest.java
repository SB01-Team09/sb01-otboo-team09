package com.part4.team09.otboo.module.domain.location.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.location.repository.GuRepository;
import com.part4.team09.otboo.module.domain.location.repository.LocationRepository;
import com.part4.team09.otboo.module.domain.location.repository.SidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class LocationBatchTest {

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  private LocationRepository locationRepository;

  @Autowired
  private SidoRepository sidoRepository;

  @Autowired
  private GuRepository guRepository;

  @Autowired
  private DongRepository dongRepository;

  @Autowired
  @Qualifier("locationJob")
  private Job locationJob;  // locationJob 빈 주입

  @BeforeEach
  void setUp() {
    jobLauncherTestUtils.setJob(locationJob);  // 명시적으로 주입
  }

  @Test
  void locationStep_should_process_and_write_locations() {
    // given

    // when
    JobExecution execution = jobLauncherTestUtils.launchStep("locationStep");

    // then
    assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    assertEquals(3560, locationRepository.count());
  }
}
