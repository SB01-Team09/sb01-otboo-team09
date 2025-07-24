package com.part4.team09.otboo.module.domain.clothes.repository.custom;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeRowDto;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.recommendation.dto.ClothingOption;
import java.util.List;
import java.util.UUID;

public interface CustomClothesRepository {

  List<ClothesAttributeRowDto> findByCursor(String cursor, UUID idAfter, int limit,
    ClothesType typeEqual, UUID ownerId, String sortBy, SortDirection sortDirection);

  List<ClothesAttributeRowDto> findByClothesId(UUID clothesId);

  List<Clothes> findAllOrderedByAttributeScores(List<ClothingOption> options, int limit);
}
