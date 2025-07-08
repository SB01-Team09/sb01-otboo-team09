package com.part4.team09.otboo.module.domain.feed.mapper;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OotdMapper {

  public OotdDto toDto(Clothes clothes, List<ClothesAttributeWithDefDto> attributes) {
    return new OotdDto(
        clothes.getId(),
        clothes.getName(),
        clothes.getImageUrl(),
        clothes.getType(),
        attributes
    );
  }
}
