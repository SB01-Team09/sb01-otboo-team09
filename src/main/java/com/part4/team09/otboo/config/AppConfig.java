package com.part4.team09.otboo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
@EnableJpaAuditing
public class AppConfig {

}
