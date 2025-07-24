package com.part4.team09.otboo.module.domain.location.repository;

import com.part4.team09.otboo.module.domain.location.entity.Gu;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuRepository extends JpaRepository<Gu, UUID> {

  Optional<Gu> findByGuName(String guName);

}
