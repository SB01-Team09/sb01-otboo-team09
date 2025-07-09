package com.part4.team09.otboo.module.domain.location.repository;

import com.part4.team09.otboo.module.domain.location.entity.Location;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends JpaRepository<Location, String> {

  @Query("""
        SELECT CASE WHEN COUNT(l) > 0 THEN TRUE ELSE FALSE END
        FROM Location l
        WHERE l.id NOT IN (
            SELECT DISTINCT w.locationId
            FROM Weather w
            WHERE w.createdAt >= :today
        )
    """)
  boolean existsLocationNotInWeather(@Param("today") LocalDateTime today);

}
