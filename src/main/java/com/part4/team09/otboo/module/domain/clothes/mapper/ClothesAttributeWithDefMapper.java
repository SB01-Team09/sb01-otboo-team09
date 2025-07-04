package com.part4.team09.otboo.module.domain.clothes.mapper;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ClothesAttributeWithDefMapper {

  public ClothesAttributeWithDefDto toDto(UUID definitionId, String definitionName,
      List<String> selectableValues, String value) {

    return new ClothesAttributeWithDefDto(definitionId, definitionName, selectableValues, value);
  }
}
