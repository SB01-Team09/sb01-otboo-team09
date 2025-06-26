package com.part4.team09.otboo.module.domain.feed.dto;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import java.util.List;
import java.util.UUID;

public record OotdDto(
    UUID clothesId,
    String name,
    String imageUrl,
    ClothesType type,
    // TODO: ClothesAttributeWithDefDto로 변경
    List<?> attributes
) {

}
