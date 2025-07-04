package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.exception.Clothes.ClothesNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClothesAttributeServiceTest {

  @InjectMocks
  private ClothesAttributeService clothesAttributeService;

  @Mock
  private ClothesAttributeRepository clothesAttributeRepository;

  @Mock
  private ClothesRepository clothesRepository;

  @Nested
  @DisplayName("의상 속성 값 - 의상 연관 생성")
  class Create {

    @Test
    @DisplayName("성공")
    void create_success() {
      // given
      UUID clothesId = UUID.randomUUID();
      UUID valueId1 = UUID.randomUUID();
      UUID valueId2 = UUID.randomUUID();

      List<UUID> selectedValueIds = List.of(valueId1, valueId2);

      ClothesAttribute attr1 = ClothesAttribute.create(clothesId, valueId1);
      ClothesAttribute attr2 = ClothesAttribute.create(clothesId, valueId2);

      given(clothesRepository.findById(clothesId)).willReturn(Optional.of(mock(Clothes.class)));
      given(clothesAttributeRepository.saveAll(anyList())).willReturn(List.of(attr1, attr2));

      // when
      List<ClothesAttribute> result = clothesAttributeService.create(clothesId, selectedValueIds);

      // then
      then(clothesRepository).should().findById(clothesId);
      then(clothesAttributeRepository).should().saveAll(anyList());

      assertEquals(result.size(), 2);
//      assertThat(result).extracting(ClothesAttribute::getClothesId).containsOnly(clothesId);
//      assertThat(result).extracting(ClothesAttribute::getSelectableValueId)
//          .containsExactlyInAnyOrder(valueId1, valueId2);
    }

    @Test
    @DisplayName("selectedValueIds가 비어있을 경우 빈 리스트 반환")
    void create_empty_selectedValueIds() {
      // given
      UUID clothesId = UUID.randomUUID();
      List<UUID> selectedValueIds = List.of();

      // when
      List<ClothesAttribute> result = clothesAttributeService.create(clothesId, selectedValueIds);

      // then
      then(clothesRepository).shouldHaveNoInteractions();
      then(clothesAttributeRepository).shouldHaveNoInteractions();

      assertEquals(result, List.of());
    }

    @Test
    @DisplayName("의상 ID가 존재하지 않을 경우 예외 발생")
    void create_clothes_not_found() {
      // given
      UUID clothesId = UUID.randomUUID();
      List<UUID> selectedValueIds = List.of(UUID.randomUUID());

      given(clothesRepository.findById(clothesId)).willReturn(Optional.empty());

      // when & then
      assertThrows(ClothesNotFoundException.class,
          () -> clothesAttributeService.create(clothesId, selectedValueIds));

      then(clothesRepository).should().findById(clothesId);
      then(clothesAttributeRepository).shouldHaveNoInteractions();
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