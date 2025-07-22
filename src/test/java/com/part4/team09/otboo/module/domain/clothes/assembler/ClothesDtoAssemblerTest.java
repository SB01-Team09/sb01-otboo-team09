package com.part4.team09.otboo.module.domain.clothes.assembler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeRowDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesDto;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeWithDefMapper;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesMapper;
import com.part4.team09.otboo.module.domain.clothes.service.SelectableValueService;
import java.time.LocalDateTime;
import java.util.List;
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
class ClothesDtoAssemblerTest {

  @InjectMocks
  private ClothesDtoAssembler clothesDtoAssembler;

  @Mock
  private SelectableValueService selectableValueService;

  @Spy
  private ClothesMapper clothesMapper;

  @Spy
  private ClothesAttributeWithDefMapper clothesAttributeWithDefMapper;

  private Clothes clothes1;
  private Clothes clothes2;
  private ClothesAttributeDef def1;
  private ClothesAttributeDef def2;
  private SelectableValue value1;
  private SelectableValue value2;
  private SelectableValue value3;
  private SelectableValue value4;

  @BeforeEach
  void setUp() {

    UUID userId = UUID.randomUUID();

    clothes1 = Clothes.create(userId, "상의", ClothesType.TOP, null);
    clothes2 = Clothes.create(userId, "하의", ClothesType.BOTTOM, null);
    ReflectionTestUtils.setField(clothes1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(clothes2, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(clothes1, "createdAt", LocalDateTime.now());
    ReflectionTestUtils.setField(clothes2, "createdAt", LocalDateTime.now().plusSeconds(1));

    def1 = ClothesAttributeDef.create("색상");
    def2 = ClothesAttributeDef.create("사이즈");
    ReflectionTestUtils.setField(def1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(def2, "id", UUID.randomUUID());

    value1 = SelectableValue.create(def1.getId(), "레드");
    value2 = SelectableValue.create(def1.getId(), "블루");
    value3 = SelectableValue.create(def2.getId(), "S");
    value4 = SelectableValue.create(def2.getId(), "M");
    ReflectionTestUtils.setField(value1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value2, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value3, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value4, "id", UUID.randomUUID());
  }

  @Nested
  @DisplayName("ClothesDto 어셈블러")
  class Assemble {

    @Test
    @DisplayName("ClothesDto 변환 성공")
    void assemble_success() {

      // given
      List<ClothesAttributeRowDto> dtos = List.of(
          new ClothesAttributeRowDto(clothes1.getId(), clothes1.getCreatedAt(), clothes1.getOwnerId(),
              clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), def1.getId(), def1.getName(),
              value1.getItem()
          ),
          new ClothesAttributeRowDto(clothes1.getId(), clothes1.getCreatedAt(), clothes1.getOwnerId(),
              clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), def2.getId(), def2.getName(),
              value4.getItem()
          )
      );

      List<SelectableValue> selectableValues = List.of(value1, value2, value3, value4);

      given(selectableValueService.findAll()).willReturn(selectableValues);

      List<ClothesAttributeWithDefDto> attributes = List.of(
          new ClothesAttributeWithDefDto(def1.getId(), def1.getName(),
              List.of(value1.getItem(), value2.getItem()), value1.getItem()),
          new ClothesAttributeWithDefDto(def2.getId(), def2.getName(),
              List.of(value3.getItem(), value4.getItem()), value4.getItem())
      );

      ClothesDto dto = new ClothesDto(clothes1.getId(), clothes1.getOwnerId(), clothes1.getName(),
          clothes1.getImageUrl(), clothes1.getType(), clothes1.getCreatedAt(), attributes);

      // when
      ClothesDto result = clothesDtoAssembler.assemble(dtos);

      // then
      assertEquals(dto, result);

      then(clothesAttributeWithDefMapper).should(times(2)).toDto(any(UUID.class),
          anyString(), anyList(), anyString());
      then(clothesMapper).should().toDto(clothes1.getId(), clothes1.getOwnerId(), clothes1.getName(),
          clothes1.getImageUrl(), clothes1.getType(), clothes1.getCreatedAt(), attributes);
    }

    @Test
    @DisplayName("의상의 속성이 없을 경우")
    void assemble_success_without_def_id() {

      // given
      List<ClothesAttributeRowDto> dtos = List.of(
          new ClothesAttributeRowDto(clothes1.getId(), clothes1.getCreatedAt(), clothes1.getOwnerId(),
              clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), null, null,
              null
          ),
          new ClothesAttributeRowDto(clothes1.getId(), clothes1.getCreatedAt(), clothes1.getOwnerId(),
              clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), null, null,
              null
          )
      );

      List<SelectableValue> selectableValues = List.of(value1, value2, value3, value4);

      given(selectableValueService.findAll()).willReturn(selectableValues);

      List<ClothesAttributeWithDefDto> attributes = List.of();

      ClothesDto dto = new ClothesDto(clothes1.getId(), clothes1.getOwnerId(), clothes1.getName(),
          clothes1.getImageUrl(), clothes1.getType(), clothes1.getCreatedAt(), attributes);

      // when
      ClothesDto result = clothesDtoAssembler.assemble(dtos);

      // then
      assertEquals(dto, result);

      then(clothesAttributeWithDefMapper).should(times(0)).toDto(any(UUID.class),
          anyString(), anyList(), anyString());
      then(clothesMapper).should().toDto(clothes1.getId(), clothes1.getOwnerId(), clothes1.getName(),
          clothes1.getImageUrl(), clothes1.getType(), clothes1.getCreatedAt(), attributes);
    }
  }

  @Nested
  @DisplayName("ClothesDto List 어셈블러")
  class AssembleList {

    @Test
    @DisplayName("ClothesDto List 변환 성공")
    void assemble_list_success() {

      // given
      List<ClothesAttributeRowDto> dtos = List.of(
          new ClothesAttributeRowDto(clothes1.getId(), clothes1.getCreatedAt(), clothes1.getOwnerId(),
              clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), def1.getId(), def1.getName(),
              value1.getItem()
          ),
          new ClothesAttributeRowDto(clothes1.getId(), clothes1.getCreatedAt(), clothes1.getOwnerId(),
              clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), def2.getId(), def2.getName(),
              value4.getItem()
          ),
          new ClothesAttributeRowDto(clothes2.getId(), clothes2.getCreatedAt(), clothes2.getOwnerId(),
              clothes2.getName(), clothes2.getImageUrl(), clothes2.getType(), def1.getId(), def1.getName(),
              value2.getItem()
          ),
          new ClothesAttributeRowDto(clothes2.getId(), clothes2.getCreatedAt(), clothes2.getOwnerId(),
              clothes2.getName(), clothes2.getImageUrl(), clothes2.getType(), def2.getId(), def2.getName(),
              value3.getItem()
          )
      );

      List<SelectableValue> selectableValues = List.of(value1, value2, value3, value4);

      given(selectableValueService.findAll()).willReturn(selectableValues);

      List<ClothesAttributeWithDefDto> attributes1 = List.of(
          new ClothesAttributeWithDefDto(def1.getId(), def1.getName(),
              List.of(value1.getItem(), value2.getItem()), value1.getItem()),
          new ClothesAttributeWithDefDto(def2.getId(), def2.getName(),
              List.of(value3.getItem(), value4.getItem()), value4.getItem())
      );
      List<ClothesAttributeWithDefDto> attributes2 = List.of(
          new ClothesAttributeWithDefDto(def1.getId(), def1.getName(),
              List.of(value1.getItem(), value2.getItem()), value2.getItem()),
          new ClothesAttributeWithDefDto(def2.getId(), def2.getName(),
              List.of(value3.getItem(), value4.getItem()), value3.getItem())
      );
      List<ClothesDto> dto = List.of(
          new ClothesDto(clothes1.getId(), clothes1.getOwnerId(), clothes1.getName(),
              clothes1.getImageUrl(), clothes1.getType(), clothes1.getCreatedAt(), attributes1),
          new ClothesDto(clothes2.getId(), clothes2.getOwnerId(), clothes2.getName(),
              clothes2.getImageUrl(), clothes2.getType(), clothes2.getCreatedAt(), attributes2)
      );


      // when
      List<ClothesDto> result = clothesDtoAssembler.assembleList(dtos);

      // then
      assertEquals(dto, result);

      then(clothesAttributeWithDefMapper).should(times(4)).toDto(any(UUID.class),
          anyString(), anyList(), anyString());
      then(clothesMapper).should(times(2)).toDto(any(UUID.class), any(UUID.class), anyString(),
          nullable(String.class), any(ClothesType.class), any(LocalDateTime.class), anyList());

    }
  }
}