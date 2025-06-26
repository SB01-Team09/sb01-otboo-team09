package com.part4.team09.otboo.module.domain.clothes.service;


import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeDefMapper;
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
// 의상 속성 명, 속성 값을 조합해주는 클래스
public class ClothesAttributeInfoService {

  // 의상 속성 정의 관련 서비스
  private final ClothesAttributeDefService clothesAttributeDefService;

  // 의상 속성 값 관련 서비스
  private final SelectableValueService selectableValueService;
  private final ClothesAttributeDefMapper clothesAttributeDefMapper;

  // 의상 속성 정의 생성
  public ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request) {

    log.debug("의상 속정 정의 생성 시작: name = {}, values = {}", request.name(), request.selectableValues());

    ClothesAttributeDef def = clothesAttributeDefService.create(request.name());

    List<String> valueList = selectableValueService.create(def.getId(), request.selectableValues())
        .stream()
        .map(SelectableValue::getItem)
        .toList();

    log.debug("의상 속성 정의 생성 완료: defId = {}, name = {}, values = {}", def.getId(), def.getName(),
        valueList);
    return clothesAttributeDefMapper.toDto(def.getId(), def.getName(), valueList);
  }

  // 의상 속성 정의 수정
  public ClothesAttributeDefDto update(UUID defId, ClothesAttributeDefUpdateRequest request) {

    log.debug("의상 속성 정의 수정 시작: defId = {}, newName = {}, values = {}", defId, request.name(),
        request.selectableValues());

    ClothesAttributeDef def = clothesAttributeDefService.update(defId, request.name());

    List<String> valueList = selectableValueService.update(def.getId(), request.selectableValues())
        .stream()
        .map(SelectableValue::getItem)
        .toList();

    log.debug("의상 속성 정의 수정 완료: defId = {}, name = {}, values = {}", def.getId(), def.getName(),
        valueList);
    return clothesAttributeDefMapper.toDto(def.getId(), def.getName(), valueList);
  }
}
