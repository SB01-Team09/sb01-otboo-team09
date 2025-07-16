package com.part4.team09.otboo.module.domain.recommendation.dto.response;

import java.util.List;
import java.util.UUID;

public record RecommendationDto(
  UUID weatherId,
  UUID userId,
  List<RecommendationClothesDto> clothes
) {

}
