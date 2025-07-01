package com.part4.team09.otboo.module.domain.clothes.repository;

import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SelectableValueRepository extends JpaRepository<SelectableValue, UUID> {

  List<SelectableValue> findAllByAttributeDefId(UUID defId);

  List<SelectableValue> findAllByAttributeDefIdIn(List<UUID> defIds);

  void deleteAllByAttributeDefId(UUID attributeDefId);

  void deleteByIdIn(List<UUID> valueIdsForDelete);
}
