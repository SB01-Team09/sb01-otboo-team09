package com.part4.team09.otboo.module.domain.clothes.repository.custom;

import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefFindRequest;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import java.util.List;
import java.util.UUID;

public interface CustomClothesAttributeDefRepository {

  // 키워드로 속성 정의, 속성 값의 defId 탐색
  List<UUID> findDefIdsByKeyword(String keyword);

  // 속성 정의 커서 기반 페이지네이션
  List<ClothesAttributeDef> findByCursor(List<UUID> defIds, ClothesAttributeDefFindRequest request);
}
