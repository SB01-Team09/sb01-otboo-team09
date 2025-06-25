package com.part4.team09.otboo.module.domain.clothes.dto.data;

import java.util.List;
import java.util.UUID;

public record ClothesAttributeDefDto(

    // 속성 정의 id
    UUID id,

    // 속성 정의 이름
    String name,

    // 선택 가능한 값 목록
    List<String> selectableValues
) {

}
