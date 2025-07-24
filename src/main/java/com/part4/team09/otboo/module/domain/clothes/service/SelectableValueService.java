package com.part4.team09.otboo.module.domain.clothes.service;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
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

    log.debug("의상 속성 명 생성 시작: defId = {}, valuesSize = {}", defId, values.size());

    // id 검사
    getClothesAttributeDefOrThrow(defId);

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

    getClothesAttributeDefOrThrow(defId);

    List<SelectableValue> response = selectableValueRepository.findAllByAttributeDefId(defId);

    log.debug("의상 속성 정의 명으로 속성 값 조회 완료: valuesSize = {}", response.size());
    return response;
  }

  @Transactional(readOnly = true)
  public List<SelectableValue> findAll() {
    log.debug("모든 의상 속성 값 조회 시작");

    List<SelectableValue> response = selectableValueRepository.findAllByOrderByCreatedAtAsc();

    log.debug("모든 의상 속성 값 조회 완료: valuesSize = {}", response.size());
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

  private ClothesAttributeDef getClothesAttributeDefOrThrow(UUID defId) {

    return clothesAttributeDefRepository.findById(defId)
        .orElseThrow(() -> {
          log.warn("의상 속성 정의를 찾을 수 없습니다. defId = {}", defId);
          return ClothesAttributeDefNotFoundException.withId(defId);
        });
  }
}
