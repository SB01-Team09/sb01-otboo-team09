package com.part4.team09.otboo.module.domain.clothes.repository;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ClothesAttributeDefRepository extends JpaRepository<ClothesAttributeDef, UUID> {

  boolean existsByName(String name);
}
