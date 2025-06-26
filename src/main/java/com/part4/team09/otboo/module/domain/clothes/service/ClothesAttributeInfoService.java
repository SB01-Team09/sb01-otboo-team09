package com.part4.team09.otboo.module.domain.clothes.service;


import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClothesAttributeInfoService {

  // 의상 속성 정의 관련 서비스
  private final ClothesAttributeDefService clothesAttributeDefService;

  // 의상 속성 값 관련 서비스
  private final SelectableValueService selectableValueService;

  public ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request) {

    ClothesAttributeDef def = clothesAttributeDefService.create(request.name());

    List<String> valueList = selectableValueService.create(def.getId(), request.selectableValues())
      .stream()
      .map(SelectableValue::getItem)
      .toList();

    return new ClothesAttributeDefDto(def.getId(), def.getName(), valueList);
  }
}
