package com.part4.team09.otboo.module.domain.recommendation.dto.response;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import java.util.List;
import java.util.UUID;

public record RecommendationClothesDto(
  UUID clothesId,
  String name,
  String imageUrl,
  ClothesType type,
  List<RecommendationClothesAttributeDto> attributes
) {

}
