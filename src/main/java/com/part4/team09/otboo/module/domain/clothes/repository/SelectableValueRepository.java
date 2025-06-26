package com.part4.team09.otboo.module.domain.clothes.repository;

import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SelectableValueRepository extends JpaRepository<SelectableValue, UUID> {

  void deleteAllByAttributeDefId(UUID attributeDefId);
}
