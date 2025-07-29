package com.part4.team09.otboo.module.domain.feed.mapper;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.exception.SelectableValue.SelectableValueNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeWithDefMapper;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.SelectableValueRepository;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OotdDtoAssembler {

  private final OotdMapper ootdMapper;
  private final ClothesAttributeWithDefMapper clothesAttributeWithDefMapper;

  private final ClothesAttributeRepository clothesAttributeRepository;
  private final ClothesAttributeDefRepository clothesAttributeDefRepository;
  private final SelectableValueRepository selectableValueRepository;

  public List<OotdDto> assemble(List<Clothes> selectedClothes) {
    return selectedClothes.stream()
        .map(clothes ->
            ootdMapper.toDto(clothes, getAttributes(clothes.getId()))
        )
        .toList();
  }

  private List<ClothesAttributeWithDefDto> getAttributes(UUID clothesId) {
    List<ClothesAttribute> attributes = clothesAttributeRepository.findAllByClothesId(clothesId);

    return attributes.stream()
        .map(attribute -> {
          SelectableValue selectValue = selectableValueRepository.findById(attribute.getSelectableValueId())
              .orElseThrow(() -> SelectableValueNotFoundException.withId(attribute.getSelectableValueId()));

          UUID definitionId = selectValue.getAttributeDefId();
          ClothesAttributeDef clothesAttributeDef = clothesAttributeDefRepository.findById(definitionId)
              .orElseThrow(() -> ClothesAttributeDefNotFoundException.withId(definitionId));

          List<String> selectableValues = selectableValueRepository.findAllByAttributeDefId(definitionId)
              .stream()
              .map(SelectableValue::getItem)
              .toList();

          return clothesAttributeWithDefMapper.toDto(
              definitionId,
              clothesAttributeDef.getName(),
              selectableValues,
              selectValue.getItem()
          );
        })
        .toList();
  }
}
