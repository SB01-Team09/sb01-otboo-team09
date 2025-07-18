package com.part4.team09.otboo.module.domain.clothes.assembler;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeRowDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesDto;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeWithDefMapper;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothesDtoAssembler {

  private final ClothesMapper clothesMapper;
  private final ClothesAttributeWithDefMapper clothesAttributeWithDefMapper;

  public ClothesDto assemble(List<ClothesAttributeRowDto> dtos,
    List<SelectableValue> selectableValues) {

    ClothesAttributeRowDto clothesWithAttributesDto = dtos.get(0);

    if (dtos.isEmpty()) {
      return clothesMapper.toDto(
        clothesWithAttributesDto.clothesId(),
        clothesWithAttributesDto.ownerId(),
        clothesWithAttributesDto.name(),
        clothesWithAttributesDto.imageUrl(),
        clothesWithAttributesDto.type(),
        clothesWithAttributesDto.createdAt(),
        List.of());
    }
    // 의상 정의 id를 기준으로 묶기
    Map<UUID, List<ClothesAttributeRowDto>> mapByDefId = dtos.stream()
      .filter(dto -> dto.attributeDefId() != null)
      .collect(Collectors.groupingBy(ClothesAttributeRowDto::attributeDefId));

    Map<UUID, List<SelectableValue>> selectableValueMap = selectableValues.stream()
      .collect(Collectors.groupingBy(SelectableValue::getAttributeDefId));

    // 정의 id 기준으로 정렬
    List<ClothesAttributeWithDefDto> attributes = mapByDefId.entrySet().stream()
      .sorted(Map.Entry.comparingByKey())
      .map(entry -> {
        UUID defId = entry.getKey();
        List<ClothesAttributeRowDto> clothesWithAttributesDtos = entry.getValue();
        ClothesAttributeRowDto firstDto = clothesWithAttributesDtos.get(0);

        String defName = firstDto.attributeDefName();

        List<String> selectableItems = selectableValueMap.getOrDefault(defId, List.of()).stream()
          .map(SelectableValue::getItem)
          .toList();

        String selectedValueName = firstDto.selectableValueItem();

        return clothesAttributeWithDefMapper.toDto(defId, defName, selectableItems,
          selectedValueName);
      })
      .toList();

    return clothesMapper.toDto(
      clothesWithAttributesDto.clothesId(),
      clothesWithAttributesDto.ownerId(),
      clothesWithAttributesDto.name(),
      clothesWithAttributesDto.imageUrl(),
      clothesWithAttributesDto.type(),
      clothesWithAttributesDto.createdAt(),
      attributes);
  }

  public List<ClothesDto> assembleList(List<ClothesAttributeRowDto> dtos,
    List<SelectableValue> selectableValues
  ) {
    // 의상 id별로 붂기, 순서 보장
    Map<UUID, List<ClothesAttributeRowDto>> groupedDtos = dtos.stream()
      .collect(Collectors.groupingBy(
        ClothesAttributeRowDto::clothesId,
        LinkedHashMap::new,
        Collectors.toList()
      ));

    return groupedDtos.values().stream()
      .map(dtoList -> assemble(dtoList, selectableValues))
      .toList();
  }

}
