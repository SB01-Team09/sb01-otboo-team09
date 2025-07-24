package com.part4.team09.otboo.module.domain.location.repository;

import com.part4.team09.otboo.module.domain.location.entity.Coordinate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoordinateRepository extends JpaRepository<Coordinate, UUID> {

  @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
        FROM Coordinate c
        WHERE c.id NOT IN (
            SELECT DISTINCT w.coordinateId
            FROM Weather w
            WHERE w.createdAt >= :today
        )
    """)
  boolean existsCoordinateNotInWeather(@Param("today") LocalDateTime today);

  Optional<Coordinate> findByXAndY(int x, int y);

}
