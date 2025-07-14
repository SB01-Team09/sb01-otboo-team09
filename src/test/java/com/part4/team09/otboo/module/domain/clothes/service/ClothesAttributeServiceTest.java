package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.exception.Clothes.ClothesNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import java.util.List;
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
class ClothesAttributeServiceTest {

  @InjectMocks
  private ClothesAttributeService clothesAttributeService;

  @Mock
  private ClothesAttributeRepository clothesAttributeRepository;

  @Mock
  private ClothesRepository clothesRepository;

  private UUID clothesId;
  private UUID valueId1;
  private UUID valueId2;
  private ClothesAttribute clothesAttribute1;
  private ClothesAttribute clothesAttribute2;

  @BeforeEach
  void setUp() {

    clothesId = UUID.randomUUID();
    valueId1 = UUID.randomUUID();
    valueId2 = UUID.randomUUID();

    clothesAttribute1 = ClothesAttribute.create(clothesId, valueId1);
    clothesAttribute2 = ClothesAttribute.create(clothesId, valueId2);
    ReflectionTestUtils.setField(clothesAttribute1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(clothesAttribute2, "id", UUID.randomUUID());
  }

  @Nested
  @DisplayName("의상 속성 값 - 의상 연관 생성")
  class Create {

    @Test
    @DisplayName("성공")
    void create_success() {

      // given
      List<UUID> selectedValueIds = List.of(valueId1, valueId2);
      given(clothesRepository.existsById(clothesId)).willReturn(true);

      given(clothesAttributeRepository.saveAll(anyList()))
          .willReturn(List.of(clothesAttribute1, clothesAttribute2));

      // when
      List<ClothesAttribute> result = clothesAttributeService.create(clothesId, selectedValueIds);

      // then
      assertEquals(result.size(), 2);

      then(clothesRepository).should().existsById(clothesId);
      then(clothesAttributeRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("selectedValueIds가 비어있을 경우 빈 리스트 반환")
    void create_empty_selectedValueIds() {

      // given
      List<UUID> selectedValueIds = List.of();

      // when
      List<ClothesAttribute> result = clothesAttributeService.create(clothesId, selectedValueIds);

      // then
      assertEquals(result, List.of());

      then(clothesRepository).should(times(0)).existsById(clothesId);
    }

    @Test
    @DisplayName("의상 ID가 존재하지 않을 경우 예외 발생")
    void create_clothes_not_found() {

      // given
      List<UUID> selectedValueIds = List.of(UUID.randomUUID());

      given(clothesRepository.existsById(clothesId)).willReturn(false);

      // when & then
      assertThrows(ClothesNotFoundException.class,
          () -> clothesAttributeService.create(clothesId, selectedValueIds));

      then(clothesRepository).should().existsById(clothesId);
      then(clothesAttributeRepository).should(times(0)).saveAll(anyList());
    }
  }


  @Nested
  @DisplayName("의상 속성 값 - 의상 연관 의상 속성 값 id 리스트로 삭제")
  class DeleteBySelectableValueIdIn {

    @Test
    @DisplayName("의상 속성 연관 리스트 삭제 성공")
    void delete_by_selectable_value_id_in_success() {

      // given
      List<UUID> valueIds = List.of(UUID.randomUUID(), UUID.randomUUID());

      // when
      clothesAttributeService.deleteBySelectableValueIdIn(valueIds);

      // then
      then(clothesAttributeRepository).should().deleteBySelectableValueIdIn(valueIds);
    }
  }
}