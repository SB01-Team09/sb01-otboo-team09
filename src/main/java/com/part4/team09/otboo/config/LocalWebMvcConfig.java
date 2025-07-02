package com.part4.team09.otboo.config;

import java.nio.file.Paths;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ConditionalOnProperty(name = "otboo.file.storage.type", havingValue = "local")
@Configuration
public class LocalWebMvcConfig implements WebMvcConfigurer {

  // local 리소스 접근
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String absolutePath = Paths.get(System.getProperty("user.dir"), "fileStorage").toAbsolutePath()
      .toUri().toString();
    registry.addResourceHandler("/files/**")
      .addResourceLocations(absolutePath)
      .setCachePeriod(3600);
  }
}
