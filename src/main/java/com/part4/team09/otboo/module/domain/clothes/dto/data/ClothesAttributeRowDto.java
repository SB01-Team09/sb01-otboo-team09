package com.part4.team09.otboo.module.domain.clothes.dto.data;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClothesAttributeRowDto(

    // 의상 관련
    UUID clothesId,
    LocalDateTime createdAt,
    UUID ownerId,
    String name,
    String imageUrl,
    ClothesType type,

    // 의상 속성 정의 관련
    UUID attributeDefId,
    String attributeDefName,

    // 선택한 의상 속성 명
    String selectableValueItem
) {

}