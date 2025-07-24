package com.part4.team09.otboo.module.domain.clothes.mapper;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ClothesAttributeDefMapper {

  public ClothesAttributeDefDto toDto(UUID id, String name, List<String> selectableValues) {
    return new ClothesAttributeDefDto(id, name, selectableValues);
  }
}
