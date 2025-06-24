package com.part4.team09.otboo.module.domain.clothes.repository;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ClothesRepository extends JpaRepository<Clothes, UUID> {

}
