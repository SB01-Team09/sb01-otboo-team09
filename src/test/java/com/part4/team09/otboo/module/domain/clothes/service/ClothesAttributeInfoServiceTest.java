package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import java.util.List;
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

  @Nested
  @DisplayName("의상 속성 생성")
  class Create {

    @Test
    @DisplayName("생성 성공")
    void create_success() {

      // given
      ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("사이즈",
        List.of("S", "M", "L"));

      ClothesAttributeDef def = ClothesAttributeDef.create(request.name());
      List<SelectableValue> selectableValues = request.selectableValues().stream()
        .map(value -> SelectableValue.create(def.getId(), value))
        .toList();

      given(clothesAttributeDefService.create(request.name())).willReturn(def);
      given(selectableValueService.create(def.getId(), request.selectableValues())).willReturn(
        selectableValues);

      // when
      ClothesAttributeDefDto result = clothesAttributeInfoService.create(request);

      // then
      assertNotNull(result);
      assertEquals(result.name(), request.name());
      assertEquals(result.selectableValues(), request.selectableValues());
      then(clothesAttributeDefService).should().create(request.name());
      then(selectableValueService).should().create(def.getId(), request.selectableValues());
    }
  }
}