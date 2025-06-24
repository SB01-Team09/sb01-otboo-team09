package com.part4.team09.otboo.module.domain.clothes.repository;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ClothesAttributeRepository extends JpaRepository<ClothesAttribute, UUID> {

}
