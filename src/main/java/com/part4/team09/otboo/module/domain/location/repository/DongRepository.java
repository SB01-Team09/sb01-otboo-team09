package com.part4.team09.otboo.module.domain.location.repository;

import com.part4.team09.otboo.module.domain.location.entity.Dong;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DongRepository extends JpaRepository<Dong, UUID> {

  Optional<Dong> findByDongName(String dongName);

  @Query(""
    + "SELECT d.id "
    + "FROM Dong d "
    + "WHERE d.latitude = :latitude AND d.longitude = :longitude"
    + "")
  Optional<UUID> findIdByLatitudeAndLongitude(
    @Param("latitude") double latitude,
    @Param("longitude") double longitude
  );

}
