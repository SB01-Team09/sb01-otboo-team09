package com.part4.team09.otboo.module.domain.clothes.mapper;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClothesAttributeWithDefMapper {

  public ClothesAttributeWithDefDto toDto(UUID definitionId, String definitionName,
      List<String> selectableValues, String value) {

    return new ClothesAttributeWithDefDto(definitionId, definitionName, selectableValues, value);
  }

  public List<ClothesAttributeWithDefDto> toDto(List<ClothesAttributeDto> attributes,
      Map<UUID, String> defMap, Map<UUID, List<SelectableValue>> selectableValueMap) {

    return attributes.stream()
        .map(attribute -> {
          UUID defId = attribute.definitionId();
          String defName = defMap.get(defId);

          List<String> selectableItems = selectableValueMap
              .getOrDefault(defId, List.of())
              .stream()
              .map(SelectableValue::getItem)
              .toList();

          return new ClothesAttributeWithDefDto(defId, defName, selectableItems, attribute.value());
        }).toList();
  }
}
