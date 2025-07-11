package com.part4.team09.otboo.module.domain.clothes.assembler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeDefMapper;
import com.part4.team09.otboo.module.domain.clothes.service.SelectableValueService;
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
class ClothesAttributeDefDtoAssemblerTest {

  @InjectMocks
  private ClothesAttributeDefDtoAssembler clothesAttributeDefDtoAssembler;

  @Mock
  private SelectableValueService selectableValueService;

  @Spy
  private ClothesAttributeDefMapper clothesAttributeDefMapper;

  private ClothesAttributeDef def1;
  private ClothesAttributeDef def2;
  private SelectableValue value1;
  private SelectableValue value2;
  private SelectableValue value3;
  private SelectableValue value4;
  private List<ClothesAttributeDef> defs;

  @BeforeEach
  void setUp() {

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

    defs = List.of(def1, def2);
  }

  @Test
  @DisplayName("dto로 변환 성공")
  void assembler_success() {

    // given
    List<UUID> defIds = defs.stream()
        .map(ClothesAttributeDef::getId)
        .toList();

    List<SelectableValue> values = List.of(value1, value2, value3, value4);
    given(selectableValueService.findAllByAttributeDefIdIn(defIds)).willReturn(values);

    List<ClothesAttributeDefDto> dtos = List.of(
        new ClothesAttributeDefDto(def1.getId(), def1.getName(),
            List.of(value1.getItem(), value2.getItem())),
        new ClothesAttributeDefDto(def2.getId(), def2.getName(),
            List.of(value3.getItem(), value4.getItem()))
    );

    // when
    List<ClothesAttributeDefDto> result = clothesAttributeDefDtoAssembler.assemble(defs);

    // then
    assertEquals(result, dtos);
    then(selectableValueService).should().findAllByAttributeDefIdIn(defIds);
    then(clothesAttributeDefMapper).should(times(2))
        .toDto(any(UUID.class), anyString(), anyList());
  }
}