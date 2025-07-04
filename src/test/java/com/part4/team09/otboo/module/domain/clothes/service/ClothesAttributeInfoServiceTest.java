package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefFindRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.response.ClothesAttributeDefDtoCursorResponse;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeDefDtoCursorResponseMapper;
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
import org.springframework.test.util.ReflectionTestUtils;

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

  @Mock
  private ClothesAttributeDefDtoCursorResponseMapper clothesAttributeDefDtoCursorResponseMapper;

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
  @DisplayName("의상 속성 조회")
  class Find {

    @Test
    @DisplayName("의상 속성 정의 조회 성공")
    void find_by_cursor_success() {
      // given
      // def 생성
      UUID defId1 = UUID.randomUUID();
      UUID defId2 = UUID.randomUUID();
      ClothesAttributeDef def1 = ClothesAttributeDef.create("사이즈");
      ClothesAttributeDef def2 = ClothesAttributeDef.create("색상");
      ReflectionTestUtils.setField(def1, "id", defId1);
      ReflectionTestUtils.setField(def2, "id", defId2);

      // selectableValue 생성
      SelectableValue value1 = SelectableValue.create(defId1, "S");

      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(
          null, null, 2, "name", SortDirection.ASCENDING, "사이즈");

      // 키워드에 해당하는 defIds
      List<UUID> defIds = List.of(def1.getId());

      // 커서기반 페이지네이션 값
      List<ClothesAttributeDef> defs = List.of(def1);

      // defId로 selectableValue 조회 값
      List<SelectableValue> selectableValues = List.of(value1);

      ClothesAttributeDefDto dto1 = new ClothesAttributeDefDto(defId1, def1.getName(), List.of(value1.getItem()));

      ClothesAttributeDefDtoCursorResponse expectedResponse = new ClothesAttributeDefDtoCursorResponse(
          List.of(dto1), null, null, false, defs.size(), "name", SortDirection.ASCENDING);

      given(clothesAttributeDefService.findIdsByKeyword("사이즈")).willReturn(defIds);
      given(clothesAttributeDefService.findByCursor(defIds, request)).willReturn(defs);
      given(selectableValueService.findAllByAttributeDefIdIn(List.of(defId1)))
          .willReturn(selectableValues);
      given(clothesAttributeDefMapper.toDto(defId1, "사이즈", List.of("S"))).willReturn(dto1);
      given(clothesAttributeDefDtoCursorResponseMapper.toDto(List.of(dto1), null, null, false, defs.size(), "name", SortDirection.ASCENDING))
          .willReturn(expectedResponse);

      // when
      ClothesAttributeDefDtoCursorResponse result = clothesAttributeInfoService.findByCursor(request);

      // then
      assertEquals(result.data().size(), 1);
      assertFalse(result.hasNext());

      then(clothesAttributeDefService).should().findIdsByKeyword("사이즈");
      then(clothesAttributeDefService).should().findByCursor(defIds, request);
      then(selectableValueService).should().findAllByAttributeDefIdIn(List.of(defId1));
      then(clothesAttributeDefMapper).should().toDto(defId1, "사이즈", List.of("S"));
      then(clothesAttributeDefDtoCursorResponseMapper).should().toDto(List.of(dto1), null, null, false, defs.size(), "name", SortDirection.ASCENDING);
    }

    @Test
    @DisplayName("의상 속성 정의 조회 결과가 없을 경우 빈 응답 반환")
    void find_by_cursor_empty_result() {
      // given
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(
          null, null, 2, "name", SortDirection.ASCENDING, "없음");

      given(clothesAttributeDefService.findIdsByKeyword("없음")).willReturn(List.of());

      ClothesAttributeDefDtoCursorResponse expectedResponse = new ClothesAttributeDefDtoCursorResponse(
          List.of(), null, null, false, 0, "name", SortDirection.ASCENDING);

      given(clothesAttributeDefDtoCursorResponseMapper.toDto(List.of(), null, null, false, 0, "name", SortDirection.ASCENDING))
          .willReturn(expectedResponse);

      // when
      ClothesAttributeDefDtoCursorResponse result = clothesAttributeInfoService.findByCursor(request);

      // then
      assertEquals(result.data(), List.of());
      assertFalse(result.hasNext());
      assertEquals(result.totalCount(), 0);

      then(clothesAttributeDefService).should().findIdsByKeyword("없음");
      then(clothesAttributeDefDtoCursorResponseMapper).should().toDto(List.of(), null, null, false, 0, "name", SortDirection.ASCENDING);
    }
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