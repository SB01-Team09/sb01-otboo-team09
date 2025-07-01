package com.part4.team09.otboo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

@Configuration
public class RestClientConfig {

  @Bean
  @Primary
  public RestClient restClient() {
    return RestClient.create();
  }

  @Bean("weatherRestClient")
  public RestClient weatherRestClient(RestClient.Builder builder) {
    DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
    uriBuilderFactory.setEncodingMode(EncodingMode.NONE);

    return RestClient.builder()
      .uriBuilderFactory(uriBuilderFactory)
      .build();
  }
}
