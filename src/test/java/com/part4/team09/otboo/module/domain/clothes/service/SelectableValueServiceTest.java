package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.SelectableValueRepository;
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
class SelectableValueServiceTest {

  @InjectMocks
  private SelectableValueService selectableValueService;

  @Mock
  private SelectableValueRepository selectableValueRepository;

  @Mock
  private ClothesAttributeDefRepository clothesAttributeDefRepository;

  @Nested
  @DisplayName("속성 값 생성")
  class Create {

    @Test
    @DisplayName("생성 성공")
    void create_success() {

      // given
      UUID defId = UUID.randomUUID();
      List<String> values = List.of("S", "M", "L", "XL");

      List<SelectableValue> selectableValues = values.stream()
          .map(value -> SelectableValue.create(defId, value))
          .toList();

      given(clothesAttributeDefRepository.existsById(defId)).willReturn(true);
      given(selectableValueRepository.saveAll(anyList())).willReturn(selectableValues);

      // when
      List<SelectableValue> result = selectableValueService.create(defId, values);

      // then
      assertNotNull(result);
      assertEquals(result.get(0), selectableValues.get(0));
      then(clothesAttributeDefRepository).should().existsById(defId);
      then(selectableValueRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("잘못된 속성 정의 id")
    void create_invalid_id() {

      // given
      UUID defId = UUID.randomUUID();

      given(clothesAttributeDefRepository.existsById(defId)).willReturn(false);

      // when, then
      assertThrows(ClothesAttributeDefNotFoundException.class,
          () -> selectableValueService.create(defId, List.of()));
    }
  }

  @Nested
  @DisplayName("속성 값 수정")
  class Update {

    @Test
    @DisplayName("수정 성공")
    void update_success() {

      // given
      UUID defId = UUID.randomUUID();
      List<String> newValues = List.of("없음", "조금 있음", "있음");
      List<SelectableValue> selectableValues = newValues.stream()
          .map(value -> SelectableValue.create(defId, value))
          .toList();

      given(clothesAttributeDefRepository.existsById(defId)).willReturn(true);
      given(selectableValueRepository.saveAll(anyList())).willReturn(selectableValues);

      // when
      List<SelectableValue> result = selectableValueService.update(defId, newValues);

      // then
      assertNotNull(result);
      assertEquals(result, selectableValues);
      then(clothesAttributeDefRepository).should().existsById(defId);
      then(selectableValueRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("잘못된 속성 정의 id")
    void update_invalid_id() {

      // given
      UUID defId = UUID.randomUUID();

      given(clothesAttributeDefRepository.existsById(defId)).willReturn(false);

      // when, then
      assertThrows(ClothesAttributeDefNotFoundException.class,
          () -> selectableValueService.update(defId, List.of()));
    }
  }
}