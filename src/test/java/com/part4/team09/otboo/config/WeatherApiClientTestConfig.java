package com.part4.team09.otboo.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

@TestConfiguration
public class WeatherApiClientTestConfig {

  @Bean
  @Qualifier("weatherRestClient")
  public RestClient weatherRestClient(RestClient.Builder builder) {
    DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
    uriBuilderFactory.setEncodingMode(EncodingMode.NONE);

    return builder
      .uriBuilderFactory(uriBuilderFactory)
      .build();
  }

  @Bean
  public MeterRegistry meterRegistry() {
    return new SimpleMeterRegistry(); // micrometer-core가 제공하는 기본 구현
  }
}
