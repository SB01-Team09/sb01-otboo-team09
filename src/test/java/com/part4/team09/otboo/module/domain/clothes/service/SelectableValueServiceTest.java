package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.SelectableValueRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SelectableValueServiceTest {

  @InjectMocks
  private SelectableValueService selectableValueService;

  @Mock
  private SelectableValueRepository selectableValueRepository;

  @Mock
  private ClothesAttributeDefRepository clothesAttributeDefRepository;

  private UUID defId;
  private ClothesAttributeDef def;
  private SelectableValue value1;
  private SelectableValue value2;
  private SelectableValue value3;
  private SelectableValue value4;

  @BeforeEach
  void setUp() {

    defId = UUID.randomUUID();
    def = ClothesAttributeDef.create("사이즈");
    ReflectionTestUtils.setField(def, "id", defId);

    value1 = SelectableValue.create(defId, "S");
    value2 = SelectableValue.create(defId, "M");
    value3 = SelectableValue.create(defId, "L");
    value4 = SelectableValue.create(defId, "XL");
    ReflectionTestUtils.setField(value1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value2, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value3, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value4, "id", UUID.randomUUID());
  }
  @Nested
  @DisplayName("속성 값 생성")
  class Create {

    @Test
    @DisplayName("생성 성공")
    void create_success() {

      // given
      List<String> values = List.of("S", "M", "L", "XL");

      List<SelectableValue> selectableValues = List.of(value1, value2, value3, value4);

      given(clothesAttributeDefRepository.findById(defId)).willReturn(Optional.of(def));
      given(selectableValueRepository.saveAll(anyList())).willReturn(selectableValues);

      // when
      List<SelectableValue> result = selectableValueService.create(defId, values);

      // then
      assertNotNull(result);
      assertEquals(result.get(0), selectableValues.get(0));
      then(clothesAttributeDefRepository).should().findById(defId);
      then(selectableValueRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("defId가 존재하지 않을 경우 예외 처리")
    void create_invalid_id() {

      // given
      given(clothesAttributeDefRepository.findById(defId)).willReturn(Optional.empty());

      // when, then
      assertThrows(ClothesAttributeDefNotFoundException.class,
          () -> selectableValueService.create(defId, List.of()));
    }
  }

  @Nested
  @DisplayName("속성 정의 id로 속성 값 조회")
  class FindAllByAttributeDefId {

    @Test
    @DisplayName("조회 성공")
    void find_all_by_attribute_def_id_success() {

      // given
      given(clothesAttributeDefRepository.findById(defId)).willReturn(Optional.of(def));

      List<SelectableValue> selectableValues = List.of(value1, value2, value3, value4);
      given(selectableValueRepository.findAllByAttributeDefId(defId)).willReturn(selectableValues);

      // when
      List<SelectableValue> result = selectableValueService.findAllByAttributeDefId(defId);

      // then
      assertEquals(selectableValues, result);

      then(clothesAttributeDefRepository).should().findById(defId);
      then(selectableValueRepository).should().findAllByAttributeDefId(defId);
    }

    @Test
    @DisplayName("잘못된 속성 정의 id일 경우 실패")
    void find_all_by_attribute_def_id_not_found_def() {

      // given
      given(clothesAttributeDefRepository.findById(defId)).willReturn(Optional.empty());

      // when, then
      assertThrows(ClothesAttributeDefNotFoundException.class,
          () -> selectableValueService.create(defId, List.of()));
    }
  }

  @Nested
  @DisplayName("모든 속성 조회")
  class FindAll {

    @Test
    @DisplayName("속성 조회 성공")
    void find_all_success() {

      // given
      List<SelectableValue> selectableValues = List.of(value1, value2, value3, value4);
      given(selectableValueRepository.findAllByOrderByCreatedAtAsc()).willReturn(selectableValues);

      // when
      List<SelectableValue> result = selectableValueService.findAll();

      // then
      assertEquals(selectableValues, result);

      then(selectableValueRepository).should().findAllByOrderByCreatedAtAsc();
    }
  }

  @Nested
  @DisplayName("의상 속성 값 리스트 삭제")
  class DeleteByIdIn {

    @Test
    @DisplayName("삭제 성공")
    void delete_by_id_in_success() {

      // given
      List<UUID> valueIds = List.of(UUID.randomUUID(), UUID.randomUUID());

      // when
      selectableValueService.deleteByIdIn(valueIds);

      // then
      then(selectableValueRepository).should().deleteByIdIn(valueIds);
    }

    @Test
    @DisplayName("valueIds가 비어있을 경우 삭제 하지 않믐")
    void delete_by_id_in_no_value_ids() {

      // given
      List<UUID> valueIds = List.of();

      // when
      selectableValueService.deleteByIdIn(valueIds);

      // then
      then(selectableValueRepository).should(times(0)).deleteByIdIn(valueIds);
    }
  }
}