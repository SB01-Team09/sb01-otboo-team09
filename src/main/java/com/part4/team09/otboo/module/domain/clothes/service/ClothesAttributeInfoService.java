package com.part4.team09.otboo.module.domain.clothes.service;


import com.part4.team09.otboo.module.common.entity.BaseEntity;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeDefMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
// 의상 속성 명, 속성 값을 조합해주는 클래스
public class ClothesAttributeInfoService {

  // 의상 속성 정의 관련 서비스
  private final ClothesAttributeDefService clothesAttributeDefService;

  // 의상 속성 값 관련 서비스
  private final SelectableValueService selectableValueService;
  private final ClothesAttributeService clothesAttributeService;
  private final ClothesAttributeDefMapper clothesAttributeDefMapper;

  // 의상 속성 정의 생성
  public ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request) {

    log.debug("의상 속정 정의 생성 시작: name = {}, values = {}", request.name(), request.selectableValues());

    ClothesAttributeDef def = clothesAttributeDefService.create(request.name());

    List<String> valueItems = selectableValueService.create(def.getId(), request.selectableValues())
        .stream()
        .map(SelectableValue::getItem)
        .toList();

    log.debug("의상 속성 정의 생성 완료: defId = {}, name = {}, values = {}", def.getId(), def.getName(),
        valueItems);
    return clothesAttributeDefMapper.toDto(def.getId(), def.getName(), valueItems);
  }

  // 의상 속성 정의 수정
  public ClothesAttributeDefDto update(UUID defId, ClothesAttributeDefUpdateRequest request) {

    log.debug("의상 속성 정의 수정 시작: defId = {}, newName = {}, values = {}", defId, request.name(),
        request.selectableValues());

    ClothesAttributeDef def = clothesAttributeDefService.findById(defId);
    String oldName = def.getName();
    String newName = request.name();

    return newName.equals(oldName)
        ? updateWhenNameSame(def, request)
        : updateWhenNameChanged(def, request);
  }

  // 의상 속성 정의 삭제
  public void delete(UUID defId) {
    log.debug("의상 속성 정의 삭제 시작: defId = {}", defId);

    clothesAttributeDefService.findById(defId);

    List<UUID> valueIds = selectableValueService.findAllByAttributeDefId(defId).stream()
        .map(BaseEntity::getId)
        .toList();

    // 연관된 ClothesAttribute 삭제
    clothesAttributeService.deleteBySelectableValueIdIn(valueIds);

    // SelectValue 삭제
    selectableValueService.deleteByIdIn(valueIds);

    // def 삭제
    clothesAttributeDefService.delete(defId);
  }

  private ClothesAttributeDefDto updateWhenNameSame(ClothesAttributeDef def,
      ClothesAttributeDefUpdateRequest request) {

    // 1. 속성 값 가져와서 새로 수정할 값과 비교
    List<SelectableValue> oldValues = selectableValueService.findAllByAttributeDefId(def.getId());
    Set<String> newValuesSet = new HashSet<>(request.selectableValues());

    List<UUID> valueIdsForDelete = oldValues.stream()
        .filter(oldValue -> !newValuesSet.contains(oldValue.getItem()))
        .map(BaseEntity::getId)
        .toList();

    // 2. clothesAttribute 삭제
    clothesAttributeService.deleteBySelectableValueIdIn(valueIdsForDelete);

    // 3. 속성 값 새로 생성
    List<String> newValueItems = selectableValueService.updateWhenNameSame(def.getId(),
            valueIdsForDelete, request.selectableValues())
        .stream()
        .map(SelectableValue::getItem)
        .toList();

    log.debug("의상 속성 정의 수정 완료(정의 이름 변경 X): defId = {}, name = {}, values = {}", def.getId(),
        def.getName(), newValueItems);
    return clothesAttributeDefMapper.toDto(def.getId(), def.getName(), newValueItems);
  }

  private ClothesAttributeDefDto updateWhenNameChanged(ClothesAttributeDef def,
      ClothesAttributeDefUpdateRequest request) {

    List<SelectableValue> oldValues = selectableValueService.findAllByAttributeDefId(def.getId());

    // 1. 정의명 변경
    ClothesAttributeDef updatedDef = clothesAttributeDefService.update(def.getId(), request.name());

    // 2. clothesAttribute 삭제
    clothesAttributeService.deleteBySelectableValueIdIn(
        oldValues.stream().map(BaseEntity::getId).toList()
    );

    // 3. 새 속성 값 전부 생성
    List<String> newValueItems = selectableValueService.updateWhenNameChanged(updatedDef.getId(),
            request.selectableValues()).stream()
        .map(SelectableValue::getItem)
        .toList();

    log.debug("의상 속성 정의 수정 완료(정의 이름 변경 O): defId = {}, name = {}, values = {}", updatedDef.getId(),
        updatedDef.getName(), newValueItems);
    return clothesAttributeDefMapper.toDto(updatedDef.getId(), updatedDef.getName(), newValueItems);
  }
}
