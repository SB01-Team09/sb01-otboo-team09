package com.part4.team09.otboo.module.domain.location.repository;

import com.part4.team09.otboo.module.domain.location.entity.Sido;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SidoRepository extends JpaRepository<Sido, UUID> {

  Optional<Sido> findBySidoName(String sidoName);

}
