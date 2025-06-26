package com.part4.team09.otboo.module.domain.clothes.service;

import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.SelectableValueRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SelectableValueService {

  private final SelectableValueRepository selectableValueRepository;
  private final ClothesAttributeDefRepository clothesAttributeDefRepository;

  public List<SelectableValue> create(UUID defId, List<String> values) {

    clothesAttributeDefRepository.findById(defId)
      .orElseThrow(() -> ClothesAttributeDefNotFoundException.withId(defId));

    List<SelectableValue> selectableValues = values.stream()
      .map(value -> SelectableValue.create(defId, value))
      .toList();

    return selectableValueRepository.saveAll(selectableValues);
  }
}
