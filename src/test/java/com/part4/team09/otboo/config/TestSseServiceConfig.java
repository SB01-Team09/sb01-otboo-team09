package com.part4.team09.otboo.config;

import com.part4.team09.otboo.module.domain.notification.sse.SseService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestSseServiceConfig {
  @Bean
  public SseService sseService() {
    return Mockito.mock(SseService.class);
  }
}
