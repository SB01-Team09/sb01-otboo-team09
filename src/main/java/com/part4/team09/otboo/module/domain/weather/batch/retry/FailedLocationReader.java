package com.part4.team09.otboo.module.domain.weather.batch.retry;

import com.part4.team09.otboo.module.domain.location.entity.Location;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.stereotype.Component;

@Component
public class FailedLocationReader extends JpaPagingItemReader<Location> {

  public FailedLocationReader(EntityManagerFactory emf) {
    setEntityManagerFactory(emf);
    setQueryString("""
        SELECT l FROM Location l
        WHERE l.id IN (
          SELECT f.failedLocationId FROM FailedLocation f
          WHERE f.createdAt >= :today AND f.retryCount <= 3
        )
      """);
    setPageSize(10);

    setParameterValues(Map.of("today", LocalDate.now().atStartOfDay()));
  }
}
