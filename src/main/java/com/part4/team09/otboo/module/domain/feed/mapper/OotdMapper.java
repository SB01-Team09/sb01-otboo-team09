package com.part4.team09.otboo.module.domain.feed.mapper;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OotdMapper {

  @Mapping(target = "clothesId", source = "clothes.id")
  OotdDto toDto(Clothes clothes, List<ClothesAttributeWithDefDto> attributes);
}
