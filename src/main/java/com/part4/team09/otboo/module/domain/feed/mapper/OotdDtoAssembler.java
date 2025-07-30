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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OotdDtoAssembler {

  private final OotdMapper ootdMapper;
  private final ClothesAttributeWithDefMapper clothesAttributeWithDefMapper;

  private final ClothesAttributeRepository clothesAttributeRepository;
  private final ClothesAttributeDefRepository clothesAttributeDefRepository;
  private final SelectableValueRepository selectableValueRepository;

  public List<OotdDto> assemble(List<Clothes> selectedClothes) {
    log.debug("OOTD DTO assemble - 선택된 옷 개수: {}", selectedClothes.size());

    List<OotdDto> result = selectedClothes.stream()
        .map(clothes -> {
          log.debug("옷 정보 변환 중 - clothesId: {}", clothes.getId());
          return ootdMapper.toDto(clothes, getAttributes(clothes.getId()));
        })
        .toList();

    log.debug("OOTD DTO assemble 완료");

    return result;
  }

  private List<ClothesAttributeWithDefDto> getAttributes(UUID clothesId) {
    log.debug("의류 속성 조회 시작 - clothesId: {}", clothesId);

    List<ClothesAttribute> attributes = clothesAttributeRepository.findAllByClothesId(clothesId);

    List<ClothesAttributeWithDefDto> result = attributes.stream()
        .map(attribute -> {
          UUID selectableValueId = attribute.getSelectableValueId();
          SelectableValue selectValue = selectableValueRepository.findById(selectableValueId)
              .orElseThrow(() -> {
                log.warn("SelectableValue를 찾을 수 없습니다. id: {}", selectableValueId);
                return SelectableValueNotFoundException.withId(selectableValueId);
              });

          UUID definitionId = selectValue.getAttributeDefId();
          ClothesAttributeDef clothesAttributeDef = clothesAttributeDefRepository.findById(
                  definitionId)
              .orElseThrow(() -> {
                log.warn("ClothesAttributeDef를 찾을 수 없습니다. id: {}", definitionId);
                return ClothesAttributeDefNotFoundException.withId(definitionId);
              });

          List<String> selectableValues = selectableValueRepository.findAllByAttributeDefId(
                  definitionId)
              .stream()
              .map(SelectableValue::getItem)
              .toList();

          log.debug("속성 매핑 완료 - defId: {}, 선택값: {}", definitionId, selectValue.getItem());

          return clothesAttributeWithDefMapper.toDto(
              definitionId,
              clothesAttributeDef.getName(),
              selectableValues,
              selectValue.getItem()
          );
        })
        .toList();

    log.debug("의류 속성 조회 완료 - clothesId: {}", clothesId);
    return result;
  }
}
