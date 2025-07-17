package com.part4.team09.otboo.module.domain.clothes.repository;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.repository.custom.CustomClothesRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ClothesRepository extends JpaRepository<Clothes, UUID>, CustomClothesRepository {

  int countByIdIn(List<UUID> ids);

  int countByOwnerIdAndType(UUID ownerId, ClothesType typeEqual);
}
