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

    log.debug("의상 속성 명 생성 시작: defId = {}, valuesSize = {}", defId, values.size());

    // id 검사
    validateDefId(defId);

    // 속성 값 생성
    List<SelectableValue> selectableValues = values.stream()
      .map(value -> SelectableValue.create(defId, value))
      .toList();

    List<SelectableValue> response = selectableValueRepository.saveAll(selectableValues);

    log.debug("의상 속성 명 생성 완료: defId = {}, values = {}", defId,
        response.size());
    return response;
  }

  // 속성 정의 명에 해당되는 속성 값들 반환
  @Transactional(readOnly = true)
  public List<SelectableValue> findAllByAttributeDefId(UUID defId) {

    log.debug("의상 속성 정의 id로 속성 값 조회 시작: defId = {}", defId);

    validateDefId(defId);

    List<SelectableValue> response = selectableValueRepository.findAllByAttributeDefId(defId);

    log.debug("의상 속성 정의 명으로 속성 값 조회 완료: valuesSize = {}", response.size());
    return response;
  }

  // 속성 정의 id 리스트로 조회
  @Transactional(readOnly = true)
  public List<SelectableValue> findAllByAttributeDefIdIn(List<UUID> defIds) {

    log.debug("의상 속성 정의 id 리스트로 속성 값 조회 시작: defIdsSize = {}", defIds.size());

    if (defIds.isEmpty()) {
      log.debug("defIds가 비어있습니다. return = {}", List.of());
      return List.of();
    }

    List<SelectableValue> response = selectableValueRepository.findAllByAttributeDefIdIn(defIds);

    log.debug("의상 속성 정의 id 리스트로 속성 값 조회 완료: valuesSize = {}", response.size());
    return response;
  }

  public List<SelectableValue> updateWhenNameSame(UUID defId, List<UUID> valueIdsForDelete,
      List<String> newValues) {

    log.debug("의상 속성 명 수정(정의 명 수정 X) 시작: defId = {}, newValuesSize = {}", defId, newValues.size());

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

    List<SelectableValue> response = selectableValueRepository.saveAll(selectableValues);

    log.debug("의상 속성 명 수정(정의 명 수정 X) 완료: valuesSize = {}", response.size());
    return response;
  }

  public List<SelectableValue> updateWhenNameChanged(UUID defId, List<String> newValues) {

    log.debug("의상 속성 명 수정(정의 명 수정 O) 시작: defId = {}, newValuesSize = {}", defId, newValues.size());

    // id 검사
    validateDefId(defId);

    // 이전의 defId를 가진 속성 값 삭제
    selectableValueRepository.deleteAllByAttributeDefId(defId);

    // 속성 값 생성
    List<SelectableValue> selectableValues = newValues.stream()
        .map(value -> SelectableValue.create(defId, value))
        .toList();

    List<SelectableValue> response = selectableValueRepository.saveAll(selectableValues);

    log.debug("의상 속성 명 수정(정의 명 수정 O) 완료: valuesSize = {}", response.size());
    return response;
  }

  // 속성 정의 명 리스트로 삭제
  public void deleteByIdIn(List<UUID> valueIds) {
    log.debug("의상 속성 값 삭제 시작: valueIdsSize = {}", valueIds.size());

    if (!valueIds.isEmpty()) {
      selectableValueRepository.deleteByIdIn(valueIds);
      log.debug("의상 속성 값 삭제 완료");
    } else {
      log.debug("의상 속성 값 id 리스트가 비어있습니다.");
    }

  }

  // defId 유효성 검사
  private void validateDefId(UUID defId) {

    if (!clothesAttributeDefRepository.existsById(defId)) {
      log.warn("의상 속성 정의가 존재하지 않습니다. id = {}", defId);
      throw ClothesAttributeDefNotFoundException.withId(defId);
    }
  }

}
