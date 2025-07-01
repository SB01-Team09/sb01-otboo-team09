package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
import java.util.stream.Stream;
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
  private ClothesAttributeService clothesAttributeService;

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
  @DisplayName("의상 속성 찾기")
  class FindByCursor {

  }

  @Nested
  @DisplayName("의상 속성 수정")
  class Update {

    @Test
    @DisplayName("정의 명 수정 X")
    void update_when_name_same() {

      // given
      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");
      List<SelectableValue> oldValues = Stream.of("S", "M", "L")
          .map(value -> SelectableValue.create(def.getId(), value))
          .toList();

      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("사이즈",
          List.of("S", "M", "XL"));

      Set<String> newValuesSet = new HashSet<>(request.selectableValues());

      List<UUID> valueIdsForDelete = oldValues.stream()
          .filter(oldValue -> !newValuesSet.contains(oldValue.getItem()))
          .map(BaseEntity::getId)
          .toList();

      List<SelectableValue> newSelectableValues = List.of(oldValues.get(0), oldValues.get(1),
          SelectableValue.create(def.getId(), "XL"));
      List<String> newValues = List.of("S", "M", "XL");
      ClothesAttributeDefDto dto = new ClothesAttributeDefDto(def.getId(), request.name(),
          newValues);

      given(clothesAttributeDefService.findById(def.getId())).willReturn(def);
      given(selectableValueService.findAllByAttributeDefId(def.getId())).willReturn(oldValues);
      given(selectableValueService.updateWhenNameSame(def.getId(), valueIdsForDelete,
          request.selectableValues())).willReturn(newSelectableValues);
      given(clothesAttributeDefMapper.toDto(def.getId(), request.name(), newValues)).willReturn(
          dto);

      // when
      ClothesAttributeDefDto result = clothesAttributeInfoService.update(def.getId(), request);

      // then
      assertNotNull(result);
      assertEquals(result.selectableValues(), newSelectableValues.stream()
          .map(SelectableValue::getItem)
          .toList());
      then(clothesAttributeDefService).should().findById(def.getId());
      then(selectableValueService).should().findAllByAttributeDefId(def.getId());
      then(selectableValueService).should()
          .updateWhenNameSame(def.getId(), valueIdsForDelete, request.selectableValues());
      then(clothesAttributeDefMapper).should().toDto(def.getId(), request.name(), newValues);
    }

    @Test
    @DisplayName("정의 명 수정 O")
    void update_when_name_changed() {

      // given
      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");
      List<SelectableValue> oldValues = Stream.of("S", "M", "L")
          .map(value -> SelectableValue.create(def.getId(), value))
          .toList();
      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("신축성",
          List.of("없음", "조금 있음", "있음"));
      ClothesAttributeDef updatedDef = ClothesAttributeDef.create(request.name());
      List<SelectableValue> newSelectableValues = Stream.of("없음", "조금 있음", "있음")
          .map(value -> SelectableValue.create(def.getId(), value))
          .toList();
      List<String> newValues = List.of("없음", "조금 있음", "있음");
      ClothesAttributeDefDto dto = new ClothesAttributeDefDto(updatedDef.getId(), updatedDef.getName(), newValues);

      given(clothesAttributeDefService.findById(def.getId())).willReturn(def);
      given(selectableValueService.findAllByAttributeDefId(def.getId())).willReturn(oldValues);
      given(clothesAttributeDefService.update(def.getId(), request.name())).willReturn(updatedDef);
      given(selectableValueService.updateWhenNameChanged(updatedDef.getId(), request.selectableValues()))
          .willReturn(newSelectableValues);
      given(clothesAttributeDefMapper.toDto(updatedDef.getId(), request.name(), newValues)).willReturn(dto);

      // when
      ClothesAttributeDefDto result = clothesAttributeInfoService.update(def.getId(), request);

      // then
      assertNotNull(result);
      assertEquals(result.selectableValues(), newValues);
      then(clothesAttributeDefService).should().findById(def.getId());
      then(selectableValueService).should().findAllByAttributeDefId(def.getId());
      then(selectableValueService).should().updateWhenNameChanged(updatedDef.getId(), request.selectableValues());
      then(clothesAttributeDefMapper).should().toDto(def.getId(), request.name(), newValues);
    }
  }

  @Nested
  @DisplayName("의상 속성 삭제")
  class Delete {

    @Test
    @DisplayName("성공")
    void delete_success() {

      // given
      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");
      SelectableValue selectableValue1 = SelectableValue.create(def.getId(), "S");
      SelectableValue selectableValue2 = SelectableValue.create(def.getId(), "M");
      List<SelectableValue> values = List.of(selectableValue1, selectableValue2);
      List<UUID> valueIds = values.stream().map(BaseEntity::getId).toList();

      given(clothesAttributeDefService.findById(def.getId())).willReturn(def);
      given(selectableValueService.findAllByAttributeDefId(def.getId())).willReturn(values);

      // when
      clothesAttributeInfoService.delete(def.getId());

      // then
      then(clothesAttributeDefService).should().findById(def.getId());
      then(selectableValueService).should().findAllByAttributeDefId(def.getId());
      then(clothesAttributeService).should().deleteBySelectableValueIdIn(valueIds);
      then(selectableValueService).should().deleteByIdIn(valueIds);
      then(clothesAttributeDefService).should().delete(def.getId());
    }
  }
}