package com.part4.team09.otboo.module.domain.clothes.service;

import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.SelectableValueRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class SelectableValueService {

  private final SelectableValueRepository selectableValueRepository;
  private final ClothesAttributeDefRepository clothesAttributeDefRepository;

  public List<SelectableValue> create(UUID defId, List<String> values) {

    log.debug("의상 속성 명 생성 시작: defId = {}, values = {}", defId, values);

    // id 검사
    validateDefId(defId);

    // 속성 값 생성
    List<SelectableValue> selectableValues = values.stream()
      .map(value -> SelectableValue.create(defId, value))
      .toList();

    log.debug("의상 속성 명 생성 완료: defId = {}, values = {}", defId,
        selectableValues.stream()
            .map(SelectableValue::getItem)
            .toList());

    return selectableValueRepository.saveAll(selectableValues);
  }

  public List<SelectableValue> update(UUID defId, List<String> newValues) {

    log.debug("의상 속성 명 수정 시작: defId = {}, newValues = {}", defId, newValues);

    // id 검사
    validateDefId(defId);

    // 이전의 defId를 가진 속성 값 삭제
    selectableValueRepository.deleteAllByAttributeDefId(defId);

    // 속성 값 생성
    List<SelectableValue> selectableValues = newValues.stream()
        .map(value -> SelectableValue.create(defId, value)).toList();

    log.debug("의상 속성 명 수정 완료: defId = {}, values = {}", defId,
        selectableValues.stream()
            .map(SelectableValue::getItem)
            .toList());
    return selectableValueRepository.saveAll(selectableValues);
  }

  // defId 유효성 검사
  private void validateDefId(UUID defId) {

    if (!clothesAttributeDefRepository.existsById(defId)) {
      log.warn("의상 속성 정의가 존재하지 않습니다. id = {}", defId);
      throw ClothesAttributeDefNotFoundException.withId(defId);
    }
  }
}
