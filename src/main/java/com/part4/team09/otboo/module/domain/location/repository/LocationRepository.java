package com.part4.team09.otboo.module.domain.location.repository;

import com.part4.team09.otboo.module.domain.location.entity.Location;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends JpaRepository<Location, String> {

  @Query("SELECT l.id FROM Location l WHERE l.dongId = :dongId")
  Optional<String> findIdByDongId(@Param("dongId") UUID dongId);
}
