package com.part4.team09.otboo.module.domain.location.repository;

import com.part4.team09.otboo.module.domain.location.entity.LocationName;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationNameRepository extends JpaRepository<LocationName, UUID> {

}
