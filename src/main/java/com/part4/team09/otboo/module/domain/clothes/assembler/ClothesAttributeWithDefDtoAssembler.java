package com.part4.team09.otboo.module.domain.clothes.assembler;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.service.ClothesAttributeDefService;
import com.part4.team09.otboo.module.domain.clothes.service.ClothesAttributeService;
import com.part4.team09.otboo.module.domain.clothes.service.SelectableValueService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothesAttributeWithDefDtoAssembler {

  private final ClothesAttributeService clothesAttributeService;
  private final SelectableValueService selectableValueService;
  private final ClothesAttributeDefService clothesAttributeDefService;

  public List<ClothesAttributeWithDefDto> assemble(UUID clothesId) {

    // 옷과 연관된 속성 값 id 엔티티
    List<ClothesAttribute> attributes = clothesAttributeService.findByClothesId(clothesId);

    // 선택한 속성 값 id
    List<UUID> selectedValueIds = attributes.stream()
        .map(ClothesAttribute::getSelectableValueId)
        .toList();

    // 선택한 속성 값
    List<SelectableValue> selectedValues = selectableValueService.findAllByIdIn(selectedValueIds);

    // defId : valueName - 선택한 속성 값 이름
    Map<UUID, String> selectedValueNames = selectedValues.stream()
        .collect(Collectors.toMap(SelectableValue::getAttributeDefId, SelectableValue::getItem));

    // 선택한 속성 정의 id
    List<UUID> defIds = selectedValues.stream()
        .map(SelectableValue::getAttributeDefId)
        .toList();

    // 속성 정의 id의 모든 속성 값
    Map<UUID, List<SelectableValue>> selectableValues = selectableValueService.findAllByAttributeDefIdIn(
            defIds).stream()
        .collect(Collectors.groupingBy(SelectableValue::getAttributeDefId));

    // 선택한 속성 정의
    List<ClothesAttributeDef> defs = clothesAttributeDefService.findAllByIds(defIds);

    return defs.stream()
        .map(def -> new ClothesAttributeWithDefDto(
            def.getId(),
            def.getName(),
            selectableValues.get(def.getId()).stream()
                .map(SelectableValue::getItem)
                .toList(),
            selectedValueNames.get(def.getId())))
        .toList();
  }
}
