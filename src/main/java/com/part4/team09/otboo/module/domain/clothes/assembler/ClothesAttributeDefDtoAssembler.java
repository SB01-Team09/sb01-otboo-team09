package com.part4.team09.otboo.module.domain.clothes.assembler;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeDefMapper;
import com.part4.team09.otboo.module.domain.clothes.service.SelectableValueService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothesAttributeDefDtoAssembler {

  private final SelectableValueService selectableValueService;

  private final ClothesAttributeDefMapper clothesAttributeDefMapper;

  public List<ClothesAttributeDefDto> assemble(List<ClothesAttributeDef> defs) {

    if (defs.isEmpty()) {
      return List.of();
    } else {
      List<UUID> defIds = defs.stream()
          .map(ClothesAttributeDef::getId)
          .toList();

      Map<UUID, List<SelectableValue>> valueMap = selectableValueService.findAllByAttributeDefIdIn(
              defIds).stream()
          .collect(Collectors.groupingBy(SelectableValue::getAttributeDefId));

      return defs.stream().map(
              def -> clothesAttributeDefMapper.toDto(def.getId(), def.getName(),
                  valueMap.get(def.getId()).stream()
                      .map(SelectableValue::getItem)
                      .toList()))
          .toList();
    }
  }
}
