package com.part4.team09.otboo.module.domain.clothes.dto.data;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClothesWithAttributesDto(
    UUID clothesId,
    LocalDateTime createdAt,
    UUID ownerId,
    String name,
    String imageUrl,
    ClothesType type,
    UUID attributeDefId,
    String attributeDefName,
    String selectableValueItem
) {}