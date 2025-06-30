package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.SelectableValueRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
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
          .map(value -> SelectableValue.create(defId, value)).toList();

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
    @DisplayName("정의 명 수정 X - 성공")
    void update_when_name_same_success() {
      // given
      UUID defId = UUID.randomUUID();
      List<UUID> valueIdsForDelete = List.of(UUID.randomUUID(), UUID.randomUUID());
      List<String> newValues = List.of("M", "L", "XL");
      List<String> oldValues = List.of("S", "M", "L");
      List<SelectableValue> oldSelectableValues = oldValues.stream()
          .map(value -> SelectableValue.create(defId, value))
          .toList();
      Set<String> oldValueSet = new HashSet<>(oldValues);
      List<SelectableValue> selectableValues = newValues.stream()
          .filter(value -> !oldValueSet.contains(value))
          .map(value -> SelectableValue.create(defId, value))
          .toList();
      List<SelectableValue> findSelectableValues = newValues.stream()
          .map(value -> SelectableValue.create(defId, value))
          .toList();

      given(clothesAttributeDefRepository.existsById(defId)).willReturn(true);
      given(selectableValueRepository.findAllByAttributeDefId(defId)).willReturn(oldSelectableValues);
      given(selectableValueRepository.saveAll(anyList())).willReturn(selectableValues);
      given(selectableValueRepository.findAllByAttributeDefId(defId)).willReturn(findSelectableValues);

      // when
      List<SelectableValue> results = selectableValueService.updateWhenNameSame(defId, valueIdsForDelete, newValues);

      // then
      assertNotNull(results);
      assertEquals(results, findSelectableValues);
      then(clothesAttributeDefRepository).should().existsById(defId);
      then(selectableValueRepository).should().deleteByIdIn(valueIdsForDelete);
      then(selectableValueRepository).should(times(2)).findAllByAttributeDefId(defId);
      then(selectableValueRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("정의 명 수정 X - 잘못된 def id")
    void update_when_name_same_not_found_def() {
      //given
      UUID defId = UUID.randomUUID();

      given(clothesAttributeDefRepository.existsById(defId)).willReturn(false);

      // when, then
      assertThrows(ClothesAttributeDefNotFoundException.class,
          () -> selectableValueService.updateWhenNameSame(defId, List.of(), List.of()));
    }

    @Test
    @DisplayName("정의 명 수정 O - 성공")
    void update_when_name_changed_success() {

      // given
      UUID defId = UUID.randomUUID();
      List<String> newValues = List.of("M", "L", "XL");
      List<SelectableValue> selectableValues = newValues.stream()
          .map(value -> SelectableValue.create(defId, value))
          .toList();

      given(clothesAttributeDefRepository.existsById(defId)).willReturn(true);
      given(selectableValueRepository.saveAll(anyList())).willReturn(selectableValues);

      // when
      List<SelectableValue> results = selectableValueService.updateWhenNameChanged(defId, newValues);

      // then
      assertNotNull(results);
      assertEquals(results, selectableValues);
      then(clothesAttributeDefRepository).should().existsById(defId);
      then(selectableValueRepository).should().deleteAllByAttributeDefId(defId);
      then(selectableValueRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("정의 명 수정 X - 잘못된 def id")
    void update_when_name_changed_not_found_def() {
      //given
      UUID defId = UUID.randomUUID();

      given(clothesAttributeDefRepository.existsById(defId)).willReturn(false);

      // when, then
      assertThrows(ClothesAttributeDefNotFoundException.class,
          () -> selectableValueService.updateWhenNameChanged(defId, List.of()));
    }
  }

  @Nested
  @DisplayName("속성 값 def id로 찾기")
  class findAllByAttributeDefId {

    @Test
    @DisplayName("속성 값 def id로 찾기 성공")
    void find_all_by_attribute_def_id_success() {

      // given
      UUID defId = UUID.randomUUID();
      List<SelectableValue> selectableValues = Stream.of("S", "M", "L")
          .map(value -> SelectableValue.create(defId, value))
          .toList();

      given(selectableValueRepository.findAllByAttributeDefId(defId)).willReturn(selectableValues);

      // when
      List<SelectableValue> results = selectableValueService.findAllByAttributeDefId(defId);

      // then
      assertNotNull(results);
      assertEquals(results, selectableValues);
      then(selectableValueRepository).should().findAllByAttributeDefId(defId);
    }
  }
}