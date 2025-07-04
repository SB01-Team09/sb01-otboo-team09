package com.part4.team09.otboo.module.domain.clothes.dto.data;

import java.util.List;
import java.util.UUID;

public record ClothesAttributeWithDefDto(

  // 속성 정의 id
  UUID definitionId,

  // 속성 정의 이름
  String definitionName,

  // 선택 가능한 값 목록
  List<String> selectableValues,

  // 선택한 속성 값
  String value
) {

}
