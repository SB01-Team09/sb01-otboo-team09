package com.part4.team09.otboo.config;

import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ShedLockConfig {

  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(
      JdbcTemplateLockProvider.Configuration.builder()
        .withJdbcTemplate(new JdbcTemplate(dataSource))
        .usingDbTime() // DB 시간 기준 (시계 오차 방지)
        .withTableName("shedlock")
        .build()
    );
  }

}
