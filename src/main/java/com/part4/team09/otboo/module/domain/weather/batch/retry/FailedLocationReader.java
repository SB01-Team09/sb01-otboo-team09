package com.part4.team09.otboo.module.domain.weather.batch.retry;

import com.part4.team09.otboo.module.domain.location.entity.Coordinate;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.stereotype.Component;

@Component
public class FailedLocationReader extends JpaPagingItemReader<Coordinate> {

  // TODO: 고쳐야 함
  public FailedLocationReader(EntityManagerFactory emf) {
    setEntityManagerFactory(emf);
    setQueryString("""
          SELECT c
          FROM Coordinate c
          WHERE c.id NOT IN (
              SELECT DISTINCT w.coordinateId
              FROM Weather w
              WHERE w.createdAt >= :today
        )
          ORDER BY c.createdAt
      """);

    Map<String, Object> params = new HashMap<>();
    params.put("today", LocalDate.now().atStartOfDay());
    setParameterValues(params);

    setPageSize(10);
  }
}
