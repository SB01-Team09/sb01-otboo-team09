package com.part4.team09.otboo.module.domain.clothes.service;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.exception.Clothes.ClothesNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClothesAttributeService {

  private final ClothesAttributeRepository clothesAttributeRepository;
  private final ClothesRepository clothesRepository;

  public List<ClothesAttribute> create(UUID clothesId, List<UUID> selectedValueIds) {
    log.debug("의상 속성 값 - 의상 연관 생성 시작: clothesId = {}, selectedValueIdsSize = {}", clothesId,
        selectedValueIds);

    if (selectedValueIds.isEmpty()) {
      log.debug("selectedValueIds가 비어있습니다. return = {}", List.of());
      return List.of();
    }

    clothesRepository.findById(clothesId)
        .orElseThrow(() -> {
          log.warn("의상이 존재하지 않습니다. id = {}", clothesId);
          return ClothesNotFoundException.withId(clothesId);
        });

    List<ClothesAttribute> clothesAttributes = selectedValueIds.stream()
        .map(selectedValueId -> ClothesAttribute.create(clothesId, selectedValueId))
        .toList();

    List<ClothesAttribute> savedClothesAttributes = clothesAttributeRepository.saveAll(
        clothesAttributes);

    log.debug("의상 속성 값 - 의상 연관 생성 완료: clothesId = {}, savedClothesAttributesSize = {}",
        savedClothesAttributes.get(0).getClothesId(), savedClothesAttributes.size());

    return savedClothesAttributes;
  }

  public void deleteBySelectableValueIdIn(List<UUID> valueIds) {
    log.debug("의상 속성 값 - 의상 연관 삭제 시작: valueIdsSize = {}", valueIds.size());

    clothesAttributeRepository.deleteBySelectableValueIdIn(valueIds);

    log.debug("의상 속성 값 - 의상 연관 삭제 완료");
  }


}
