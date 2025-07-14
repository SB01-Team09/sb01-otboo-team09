package com.part4.team09.otboo.module.domain.recommendation.dto.response;

import java.util.List;
import java.util.UUID;

public record RecommendationClothesAttributeDto(
  UUID definitionId,
  String definitionName,
  List<String> selectableValues,
  String value
) {

}
