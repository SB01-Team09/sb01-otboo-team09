package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;

import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeRepository;
import java.util.List;
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