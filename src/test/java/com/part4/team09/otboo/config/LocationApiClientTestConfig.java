package com.part4.team09.otboo.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@TestConfiguration
public class LocationApiClientTestConfig {

  @Bean
  public RestClient locationRestClient(RestClient.Builder builder) {
    return builder.build();
  }

}
