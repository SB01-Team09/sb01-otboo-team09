package com.part4.team09.otboo.config.P6spy;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
public class P6spyMeterRegistryConfig {

  public P6spyMeterRegistryConfig(MeterRegistry meterRegistry) {
    P6spyPrettySqlFormatter.setMeterRegistry(meterRegistry);
  }
}

