package com.part4.team09.otboo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "otboo.file.storage.type", havingValue = "s3")
public class AwsS3Config {

  @Value("${otboo.file.storage.s3.region}")
  private String region;

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
      .region(Region.of(region))
      .credentialsProvider(DefaultCredentialsProvider.create())
      .build();
  }
}
