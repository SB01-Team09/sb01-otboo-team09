package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.assembler.ClothesAttributeDefDtoAssembler;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
  private ClothesAttributeDefDtoAssembler clothesAttributeDefDtoAssembler;

  @Spy
  private ClothesAttributeDefMapper clothesAttributeDefMapper;

  @Spy
  private ClothesAttributeDefDtoCursorResponseMapper clothesAttributeDefDtoCursorResponseMapper;

  private ClothesAttributeDef def1;
  private ClothesAttributeDef def2;
  private SelectableValue value1;
  private SelectableValue value2;
  private SelectableValue value3;
  private SelectableValue value4;
  private SelectableValue value5;
  private SelectableValue value6;

  @BeforeEach
  void setUp() {

    def1 = ClothesAttributeDef.create("사이즈");
    ReflectionTestUtils.setField(def1, "id", UUID.randomUUID());

    value1 = SelectableValue.create(def1.getId(), "S");
    value2 = SelectableValue.create(def1.getId(), "M");
    value3 = SelectableValue.create(def1.getId(), "L");
    ReflectionTestUtils.setField(value1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value2, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value3, "id", UUID.randomUUID());

    def2 = ClothesAttributeDef.create("색상");
    ReflectionTestUtils.setField(def2, "id", UUID.randomUUID());

    value4 = SelectableValue.create(def2.getId(), "레드");
    value5 = SelectableValue.create(def2.getId(), "블랙");
    value6 = SelectableValue.create(def2.getId(), "사파이어");
    ReflectionTestUtils.setField(value4, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value5, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value6, "id", UUID.randomUUID());
  }

  @Nested
  @DisplayName("의상 속성 생성")
  class Create {

    @Test
    @DisplayName("생성 성공")
    void create_success() {

      // given
      ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(def1.getName(),
          List.of(value1.getItem(), value2.getItem(), value3.getItem()));
      List<SelectableValue> selectableValues = List.of(value1, value2, value3);

      given(clothesAttributeDefService.create(request.name())).willReturn(def1);
      given(selectableValueService.create(def1.getId(), request.selectableValues())).willReturn(
          selectableValues);

      // when
      ClothesAttributeDefDto result = clothesAttributeInfoService.create(request);

      // then
      assertNotNull(result);
      assertEquals(result.name(), request.name());
      assertEquals(result.selectableValues(), request.selectableValues());
      then(clothesAttributeDefService).should().create(request.name());
      then(selectableValueService).should().create(def1.getId(), request.selectableValues());
    }
  }

  @Nested
  @DisplayName("의상 속성 조회")
  class Find {

    @Test
    @DisplayName("의상 속성 정의 조회 성공")
    void find_by_cursor_success() {

      // given
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(
          null, null, 2, "name", SortDirection.ASCENDING, "사이즈");

      // 키워드에 해당하는 defIds
      List<UUID> defIds = List.of(def1.getId());
      given(clothesAttributeDefService.findIdsByKeyword(request.keywordLike())).willReturn(defIds);

      // 커서기반 페이지네이션 값
      List<ClothesAttributeDef> defs = List.of(def1);
      given(clothesAttributeDefService.findByCursor(defIds, request)).willReturn(defs);

      List<ClothesAttributeDefDto> data = List.of(clothesAttributeDefMapper.toDto(def1.getId(), def1.getName(),
          List.of(value1.getItem(), value2.getItem(), value3.getItem())));
      given(clothesAttributeDefDtoAssembler.assemble(defs)).willReturn(data);

      // when
      ClothesAttributeDefDtoCursorResponse result = clothesAttributeInfoService.findByCursor(request);

      // then
      assertEquals(result.data().size(), 1);
      assertFalse(result.hasNext());

      then(clothesAttributeDefService).should().findIdsByKeyword(request.keywordLike());
      then(clothesAttributeDefService).should().findByCursor(defIds, request);
      then(clothesAttributeDefDtoAssembler).should().assemble(defs);
    }

    @Test
    @DisplayName("의상 속성 정의 조회 성공 - next가 존재")
    void find_by_cursor_success_exists_next() {

      // given
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(
          null, null, 1, "name", SortDirection.ASCENDING, "사");

      List<UUID> defIds = List.of(def1.getId(), def1.getId());
      given(clothesAttributeDefService.findIdsByKeyword(request.keywordLike())).willReturn(defIds);

      List<ClothesAttributeDef> defs = List.of(def1, def2);
      given(clothesAttributeDefService.findByCursor(defIds, request)).willReturn(defs);

      List<ClothesAttributeDefDto> data = List.of(clothesAttributeDefMapper.toDto(def1.getId(),
          def1.getName(), List.of(value1.getItem(), value2.getItem(), value3.getItem())));
      List<ClothesAttributeDef> pagedDefs = List.of(def1);
      given(clothesAttributeDefDtoAssembler.assemble(pagedDefs)).willReturn(data);

      // when
      ClothesAttributeDefDtoCursorResponse result = clothesAttributeInfoService.findByCursor(request);

      // then
      assertEquals(1, result.data().size());
      assertTrue(result.hasNext());
      assertEquals(def1.getName(), result.nextCursor());
      assertEquals(def1.getId(), result.nextIdAfter());

      then(clothesAttributeDefService).should().findIdsByKeyword(request.keywordLike());
      then(clothesAttributeDefService).should().findByCursor(defIds, request);
      then(clothesAttributeDefDtoAssembler).should().assemble(pagedDefs);
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

      // when
      ClothesAttributeDefDtoCursorResponse result = clothesAttributeInfoService.findByCursor(request);

      // then
      assertEquals(result.data(), List.of());
      assertFalse(result.hasNext());
      assertEquals(result.totalCount(), 0);

      then(clothesAttributeDefService).should().findIdsByKeyword("없음");
    }
  }

  @Nested
  @DisplayName("의상 속성 수정")
  class Update {

    @Test
    @DisplayName("정의 명 수정 X")
    void update_when_name_same() {

      // given
      UUID defId = def1.getId();
      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest(def1.getName(),
          List.of("S", "M", "XL"));

      given(clothesAttributeDefService.findById(defId)).willReturn(def1);

      List<SelectableValue> oldValues = List.of(value1, value2, value3);

      Set<String> newValuesSet = new HashSet<>(request.selectableValues());
      List<UUID> valueIdsForDelete = oldValues.stream()
          .filter(oldValue -> !newValuesSet.contains(oldValue.getItem()))
          .map(BaseEntity::getId)
          .toList();
      System.out.println("valueIdsForDelete: " + valueIdsForDelete);
      List<SelectableValue> newSelectableValues = List.of(SelectableValue.create(defId, "XL"));
      given(selectableValueService.updateWhenNameSame(defId, valueIdsForDelete,
          request.selectableValues())).willReturn(newSelectableValues);

      List<SelectableValue> values = List.of(value1, value2, newSelectableValues.get(0));
      given(selectableValueService.findAllByAttributeDefId(defId))
          .willReturn(oldValues)    // 첫 호출 때 (delete 대상 판단용)
          .willReturn(values);

      // when
      ClothesAttributeDefDto result = clothesAttributeInfoService.update(defId, request);

      // then
      assertNotNull(result);
      assertEquals(result.selectableValues(), values.stream()
          .map(SelectableValue::getItem)
          .toList());
      then(clothesAttributeDefService).should().findById(defId);
      then(selectableValueService).should(times(2)).findAllByAttributeDefId(defId);
      then(selectableValueService).should()
          .updateWhenNameSame(defId, valueIdsForDelete, request.selectableValues());
      then(clothesAttributeService).should().deleteBySelectableValueIdIn(valueIdsForDelete);
    }

    @Test
    @DisplayName("정의 명 수정 O")
    void update_when_name_changed() {

      // given
      UUID defId = def1.getId();
      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("newName",
          List.of("S", "M", "XL"));

      given(clothesAttributeDefService.findById(defId)).willReturn(def1);

      List<SelectableValue> oldValues = List.of(value1, value2, value3);
      given(selectableValueService.findAllByAttributeDefId(defId)).willReturn(oldValues);

      ClothesAttributeDef updatedDef = ClothesAttributeDef.create(request.name());
      ReflectionTestUtils.setField(updatedDef, "id", UUID.randomUUID());
      given(clothesAttributeDefService.update(defId, request.name())).willReturn(updatedDef);

      List<SelectableValue> newSelectableValues = List.of(
          SelectableValue.create(defId, "S"),
          SelectableValue.create(defId, "M"),
          SelectableValue.create(defId, "XL")
      );
      given(selectableValueService.updateWhenNameChanged(updatedDef.getId(), request.selectableValues()))
          .willReturn(newSelectableValues);

      // when
      ClothesAttributeDefDto result = clothesAttributeInfoService.update(defId, request);

      // then
      assertNotNull(result);
      assertEquals(request.selectableValues(), result.selectableValues());
      then(clothesAttributeDefService).should().findById(defId);
      then(selectableValueService).should().findAllByAttributeDefId(defId);
      then(clothesAttributeDefService).should().update(defId, request.name());
      then(clothesAttributeService).should()
          .deleteBySelectableValueIdIn(oldValues.stream().map(BaseEntity::getId).toList());
      then(selectableValueService).should().updateWhenNameChanged(updatedDef.getId(), request.selectableValues());
    }
  }

  @Nested
  @DisplayName("의상 속성 삭제")
  class Delete {

    @Test
    @DisplayName("성공")
    void delete_success() {

      // given
      UUID defId = def1.getId();
      given(clothesAttributeDefService.findById(defId)).willReturn(def1);

      List<SelectableValue> values = List.of(value1, value2, value3);
      given(selectableValueService.findAllByAttributeDefId(defId)).willReturn(values);

      List<UUID> valueIds = values.stream()
          .map(BaseEntity::getId)
          .toList();

      // when
      clothesAttributeInfoService.delete(defId);

      // then
      then(clothesAttributeDefService).should().findById(defId);
      then(selectableValueService).should().findAllByAttributeDefId(defId);
      then(clothesAttributeService).should().deleteBySelectableValueIdIn(valueIds);
      then(selectableValueService).should().deleteByIdIn(valueIds);
      then(clothesAttributeDefService).should().delete(defId);
    }
  }
}