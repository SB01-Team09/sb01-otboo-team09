package com.part4.team09.otboo.module.domain.clothes.repository;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.repository.custom.CustomClothesAttributeDefRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ClothesAttributeDefRepository extends JpaRepository<ClothesAttributeDef, UUID>,
  CustomClothesAttributeDefRepository {

  boolean existsByName(String name);
}
