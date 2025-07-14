package com.part4.team09.otboo.module.domain.clothes.assembler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeWithDefMapper;
import com.part4.team09.otboo.module.domain.clothes.service.ClothesAttributeDefService;
import com.part4.team09.otboo.module.domain.clothes.service.ClothesAttributeService;
import com.part4.team09.otboo.module.domain.clothes.service.SelectableValueService;
import com.part4.team09.otboo.module.domain.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClothesAttributeWithDefDtoAssemblerTest {

  @InjectMocks
  private ClothesAttributeWithDefDtoAssembler clothesAttributeWithDefDtoAssembler;

  @Mock
  private ClothesAttributeService clothesAttributeService;

  @Mock
  private SelectableValueService selectableValueService;

  @Mock
  private ClothesAttributeDefService clothesAttributeDefService;

  @Spy
  private ClothesAttributeWithDefMapper clothesAttributeWithDefMapper;

  private User user;
  private ClothesAttributeDef def1;
  private ClothesAttributeDef def2;
  private SelectableValue value1;
  private SelectableValue value2;
  private SelectableValue value3;
  private SelectableValue value4;
  private Clothes clothes1;
  private ClothesAttribute clothesAttribute1;
  private ClothesAttribute clothesAttribute2;


  @BeforeEach
  void setUp() {

    user = User.createUser("test@gmail.com", "test", "qwer1234!");
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

    def1 = ClothesAttributeDef.create("사이즈");
    def2 = ClothesAttributeDef.create("색상");
    ReflectionTestUtils.setField(def1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(def2, "id", UUID.randomUUID());

    value1 = SelectableValue.create(def1.getId(), "S");
    value2 = SelectableValue.create(def1.getId(), "M");
    value3 = SelectableValue.create(def2.getId(), "레드");
    value4 = SelectableValue.create(def2.getId(), "블랙");
    ReflectionTestUtils.setField(value1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value2, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value3, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value4, "id", UUID.randomUUID());

    clothes1 = Clothes.create(user.getId(), "티셔츠", ClothesType.TOP, null);
    ReflectionTestUtils.setField(clothes1, "id", UUID.randomUUID());

    clothesAttribute1 = ClothesAttribute.create(clothes1.getId(), value1.getId());
    clothesAttribute2 = ClothesAttribute.create(clothes1.getId(), value3.getId());
    ReflectionTestUtils.setField(clothesAttribute1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(clothesAttribute2, "id", UUID.randomUUID());
  }

  @Test
  @DisplayName("dto로 변환 성공")
  void assembler_success() {

    // given
    UUID clothesId = clothes1.getId();
    List<ClothesAttribute> attributes = List.of(clothesAttribute1, clothesAttribute2);
    given(clothesAttributeService.findByClothesId(clothesId)).willReturn(attributes);

    List<SelectableValue> selectedValues = List.of(value1, value3);
    List<UUID> selectedValueIds = attributes.stream()
        .map(ClothesAttribute::getSelectableValueId)
        .toList();
    given(selectableValueService.findAllByIdIn(selectedValueIds))
        .willReturn(selectedValues);

    List<UUID> defIds = selectedValues.stream()
        .map(SelectableValue::getAttributeDefId)
        .toList();
    List<SelectableValue> selectableValues = List.of(value1, value2, value3, value4);
    given(selectableValueService.findAllByAttributeDefIdIn(defIds))
        .willReturn(selectableValues);

    List<ClothesAttributeDef> defs = List.of(def1, def2);
    given(clothesAttributeDefService.findAllByIds(defIds)).willReturn(defs);

    List<ClothesAttributeWithDefDto> dtos = List.of(
        new ClothesAttributeWithDefDto(def1.getId(), def1.getName(), List.of(value1.getItem(),
            value2.getItem()), value1.getItem()),
        new ClothesAttributeWithDefDto(def2.getId(), def2.getName(), List.of(value3.getItem(),
            value4.getItem()), value3.getItem())
    );

    // when
    List<ClothesAttributeWithDefDto> result = clothesAttributeWithDefDtoAssembler.assemble(clothesId);

    // then
    assertEquals(result, dtos);

    then(clothesAttributeService).should().findByClothesId(clothesId);
    then(selectableValueService).should().findAllByIdIn(selectedValueIds);
    then(selectableValueService).should().findAllByAttributeDefIdIn(defIds);
    then(clothesAttributeDefService).should().findAllByIds(defIds);
  }
}