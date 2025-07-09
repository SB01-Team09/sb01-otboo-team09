package com.part4.team09.otboo.module.domain.feed.service;

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
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.SelectableValueRepository;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.entity.Ootd;
import com.part4.team09.otboo.module.domain.feed.mapper.OotdMapper;
import com.part4.team09.otboo.module.domain.feed.repository.OotdRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OotdService {

  private final OotdRepository ootdRepository;
  private final OotdMapper ootdMapper;

  private final ClothesRepository clothesRepository;
  private final ClothesAttributeDefRepository clothesAttributeDefRepository;
  private final ClothesAttributeRepository clothesAttributeRepository;
  private final SelectableValueRepository selectableValueRepository;

  private final ClothesAttributeWithDefMapper clothesAttributeWithDefMapper;

  @Transactional
  public void create(UUID feedId, List<UUID> clothesIds) {
    validateAllClothesExist(clothesIds);
    List<Ootd> ootds = clothesIds.stream()
        .map(clothesId -> Ootd.create(feedId, clothesId))
        .toList();

    ootdRepository.saveAll(ootds);
  }

  @Transactional(readOnly = true)
  public List<OotdDto> getOotds(UUID feedID) {
    List<UUID> clothesIds = ootdRepository.findClothesIdsByFeedId(feedID);
    List<Clothes> selectedClothes = getAllByClothesIdsOrThrow(clothesIds);

    return selectedClothes.stream()
        .map(clothes ->
            ootdMapper.toDto(clothes, getAttributes(clothes.getId()))
        )
        .toList();
  }

  @Transactional
  public void deleteAllByFeedId(UUID feedId) {
    ootdRepository.deleteAllByFeedId(feedId);
  }

  private void validateAllClothesExist(List<UUID> clothesIds) {
    int foundCount = clothesRepository.countByIdIn(clothesIds);

    // TODO: 의상 커스텀 예외로 변경
    if (foundCount != clothesIds.size()) {
      throw new EntityNotFoundException("");
    }
  }

  private List<Clothes> getAllByClothesIdsOrThrow(List<UUID> clothesIds) {
    List<Clothes> foundClothes = clothesRepository.findAllById(clothesIds);

    List<UUID> foundIds = foundClothes.stream()
        .map(Clothes::getId)
        .toList();

    List<UUID> missingIds = clothesIds.stream()
        .filter(id -> !foundIds.contains(id))
        .toList();

    // TODO: 의상 커스텀 예외로 변경
    if (!missingIds.isEmpty()) {
      throw new EntityNotFoundException();
    }

    return foundClothes;
  }

  private List<ClothesAttributeWithDefDto> getAttributes(UUID clothesId) {
    List<ClothesAttribute> attributes = clothesAttributeRepository.findAllByClothesId(clothesId);

    List<ClothesAttributeWithDefDto> clothesAttributeWithDefs = attributes.stream()
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

    return clothesAttributeWithDefs;
  }
}
