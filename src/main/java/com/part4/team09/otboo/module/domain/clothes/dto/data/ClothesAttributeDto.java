package com.part4.team09.otboo.module.domain.clothes.dto.data;

import java.util.UUID;

public record ClothesAttributeDto(

  // 속성 정의 id
  UUID definitionId,

  // 선택한 속성 값
  String value
) {

}
