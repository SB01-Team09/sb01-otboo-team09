package com.part4.team09.otboo.module.domain.clothes.mapper;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesDto;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ClothesMapper {

  public ClothesDto toDto(UUID id, UUID ownerId, String name, String imageUrl, ClothesType type,
      List<ClothesAttributeWithDefDto> attributes) {

    return new ClothesDto(id, ownerId, name, imageUrl, type, attributes);
  }
}
