package com.part4.team09.otboo.module.domain.location.repository;

import com.part4.team09.otboo.module.domain.location.entity.Dong;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DongRepository extends JpaRepository<Dong, UUID> {

  Optional<Dong> findByDongName(String dongName);

}
