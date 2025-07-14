package com.part4.team09.otboo.module.domain.clothes.dto.request;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDto;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import java.util.List;

public record ClothesUpdateRequest(

  // 의상 이름 - 변경이 없으면 null
  String name,

  // 의상 타입 - 변경이 없으면 null
  ClothesType type,

  // 의상 속성 - 모든 속
  List<ClothesAttributeDto> attributes
) {

}
