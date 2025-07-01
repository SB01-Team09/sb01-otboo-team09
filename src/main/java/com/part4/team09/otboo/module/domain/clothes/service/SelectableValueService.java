package com.part4.team09.otboo.module.domain.clothes.service;

import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.SelectableValueRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

  public List<SelectableValue> updateWhenNameSame(UUID defId, List<UUID> valueIdsForDelete,
      List<String> newValues) {

    log.debug("의상 속성 명 수정(정의 명 수정 X) 시작: defId = {}, newValues = {}", defId, newValues);

    // id 검사
    validateDefId(defId);

    // 삭제할 속성 삭제
    selectableValueRepository.deleteByIdIn(valueIdsForDelete);

    List<String> oldValues = selectableValueRepository.findAllByAttributeDefId(defId).stream()
        .map(SelectableValue::getItem)
        .toList();
    Set<String> oldValueSet = new HashSet<>(oldValues);

    // 속성 값 생성
    List<SelectableValue> selectableValues = newValues.stream()
        .filter(value -> !oldValueSet.contains(value))
        .map(value -> SelectableValue.create(defId, value))
        .toList();

    log.debug("의상 속성 명 수정(정의 명 수정 X) 완료: defId = {}, values = {}", defId,
        selectableValues.stream()
            .map(SelectableValue::getItem)
            .toList());
    selectableValueRepository.saveAll(selectableValues);
    return selectableValueRepository.findAllByAttributeDefId(defId);
  }

  public List<SelectableValue> updateWhenNameChanged(UUID defId, List<String> newValues) {

    log.debug("의상 속성 명 수정(정의 명 수정 O) 시작: defId = {}, newValues = {}", defId, newValues);

    // id 검사
    validateDefId(defId);

    // 이전의 defId를 가진 속성 값 삭제
    selectableValueRepository.deleteAllByAttributeDefId(defId);

    // 속성 값 생성
    List<SelectableValue> selectableValues = newValues.stream()
        .map(value -> SelectableValue.create(defId, value))
        .toList();

    log.debug("의상 속성 명 수정(정의 명 수정 O)  완료: defId = {}, values = {}", defId,
        selectableValues.stream()
            .map(SelectableValue::getItem)
            .toList());
    return selectableValueRepository.saveAll(selectableValues);
  }

  // 속성 정의 명에 해당되는 속성 값들 반환
  public List<SelectableValue> findAllByAttributeDefId(UUID defId) {

    return selectableValueRepository.findAllByAttributeDefId(defId);
  }

  // 속성 정의 명 리스트로 삭제
  public void deleteByIdIn(List<UUID> valueIds) {
    log.debug("의상 속성 값 삭제: valueIds = {}", valueIds);

    selectableValueRepository.deleteByIdIn(valueIds);
  }

  // defId 유효성 검사
  private void validateDefId(UUID defId) {

    if (!clothesAttributeDefRepository.existsById(defId)) {
      log.warn("의상 속성 정의가 존재하지 않습니다. id = {}", defId);
      throw ClothesAttributeDefNotFoundException.withId(defId);
    }
  }
}
