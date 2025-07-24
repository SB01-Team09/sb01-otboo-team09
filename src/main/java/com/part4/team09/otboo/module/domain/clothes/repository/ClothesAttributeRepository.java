package com.part4.team09.otboo.module.domain.clothes.repository;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ClothesAttributeRepository extends JpaRepository<ClothesAttribute, UUID> {

  List<ClothesAttribute> findAllByClothesId(UUID clothesId);

  void deleteBySelectableValueIdIn(List<UUID> oldValueIds);

  void deleteAllByClothesId(UUID clothesId);
}
