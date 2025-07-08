package com.part4.team09.otboo.module.domain.weather.batch.retry;

import com.part4.team09.otboo.module.domain.location.entity.Location;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.stereotype.Component;

@Component
public class FailedLocationReader extends JpaPagingItemReader<Location> {

  public FailedLocationReader(EntityManagerFactory emf) {
    setEntityManagerFactory(emf);
    setQueryString("""
          SELECT l
          FROM Location l
          WHERE l.id NOT IN (
              SELECT DISTINCT w.locationId
              FROM Weather w
              WHERE w.createdAt >= :today
        )
      """);

    Map<String, Object> params = new HashMap<>();
    params.put("today", LocalDate.now().atStartOfDay());
    setParameterValues(params);

    setPageSize(10);
  }

}
