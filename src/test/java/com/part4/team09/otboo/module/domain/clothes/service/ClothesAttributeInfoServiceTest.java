package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeDefMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClothesAttributeInfoServiceTest {

  @InjectMocks
  private ClothesAttributeInfoService clothesAttributeInfoService;

  @Mock
  private ClothesAttributeDefService clothesAttributeDefService;

  @Mock
  private SelectableValueService selectableValueService;

  @Mock
  private ClothesAttributeDefMapper clothesAttributeDefMapper;

  @Nested
  @DisplayName("의상 속성 생성")
  class Create {

    @Test
    @DisplayName("생성 성공")
    void create_success() {

      // given
      // 리퀘스트
      ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("사이즈",
          List.of("S", "M", "L"));

      // 정의 생성
      ClothesAttributeDef def = ClothesAttributeDef.create(request.name());
      // 속성값 생성
      List<SelectableValue> selectableValues = request.selectableValues().stream()
          .map(value -> SelectableValue.create(def.getId(), value))
          .toList();
      List<String> valueList = selectableValues.stream()
          .map(SelectableValue::getItem)
          .toList();
      ClothesAttributeDefDto dto = new ClothesAttributeDefDto(def.getId(), def.getName(),
          valueList);

      given(clothesAttributeDefService.create(request.name())).willReturn(def);
      given(selectableValueService.create(def.getId(), request.selectableValues())).willReturn(
          selectableValues);
      given(clothesAttributeDefMapper.toDto(def.getId(), def.getName(),
          valueList)).willReturn(dto);

      // when
      ClothesAttributeDefDto result = clothesAttributeInfoService.create(request);

      // then
      assertNotNull(result);
      assertEquals(result.name(), request.name());
      assertEquals(result.selectableValues(), request.selectableValues());
      then(clothesAttributeDefService).should().create(request.name());
      then(selectableValueService).should().create(def.getId(), request.selectableValues());
      then(clothesAttributeDefMapper).should().toDto(def.getId(), def.getName(),
          selectableValues.stream().map(SelectableValue::getItem)
              .toList());
    }
  }

  @Nested
  @DisplayName("의상 속성 수정")
  class Update {

    @Test
    @DisplayName("수정 성공")
    void update_success() {

      // given
      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("사이즈",
          List.of("S", "M", "L"));

      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");
      List<SelectableValue> selectableValues = request.selectableValues().stream()
          .map(value -> SelectableValue.create(def.getId(), value))
          .toList();
      ClothesAttributeDefDto dto = new ClothesAttributeDefDto(def.getId(), def.getName(),
          request.selectableValues());

      given(clothesAttributeDefService.update(def.getId(), request.name())).willReturn(def);
      given(selectableValueService.update(def.getId(), request.selectableValues())).willReturn(selectableValues);
      given(clothesAttributeDefMapper.toDto(def.getId(), def.getName(), request.selectableValues())).willReturn(dto);

      // when
      ClothesAttributeDefDto result = clothesAttributeInfoService.update(def.getId(), request);

      // then
      assertNotNull(result);
      assertEquals(result.id(), def.getId());
      assertEquals(result.selectableValues(), request.selectableValues());
      then(clothesAttributeDefService).should().update(def.getId(), request.name());
      then(selectableValueService).should().update(def.getId(), request.selectableValues());
      then(clothesAttributeDefMapper).should().toDto(def.getId(), request.name(), request.selectableValues());
    }
  }
}