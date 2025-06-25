package com.part4.team09.otboo.module.domain.clothes.dto.data;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import java.util.List;
import java.util.UUID;

public record ClothesDto(

    // 의상 id
    UUID id,

    // 소유자 id
    UUID ownerId,

    // 의상 이름
    String name,

    // 의상 이미지 URL
    String imageUrl,

    // 의상 타입
    ClothesType type,

    // 의상 속성
    List<ClothesAttributeWithDefDto> attributes
) {

}
