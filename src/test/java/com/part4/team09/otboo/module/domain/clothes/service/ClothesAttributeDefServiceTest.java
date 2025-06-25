package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefAlreadyExistsException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClothesAttributeDefServiceTest {

  @InjectMocks
  private ClothesAttributeDefService clothesAttributeDefService;

  @Mock
  private ClothesAttributeDefRepository clothesAttributeDefRepository;

  @Nested
  @DisplayName("의상 속성 정의 생성")
  class CreateTest {

    @Test
    @DisplayName("생성 성공")
    void create_success() {

      // given
      String name = "사이즈";
      ClothesAttributeDef def = ClothesAttributeDef.create(name);

      given(clothesAttributeDefRepository.existsByName(name)).willReturn(false);
      given(clothesAttributeDefRepository.save(any(ClothesAttributeDef.class))).willReturn(def);

      // when
      ClothesAttributeDef result = clothesAttributeDefService.create(name);

      // then
      assertNotNull(result);
      assertEquals(result.getName(), def.getName());
      then(clothesAttributeDefRepository).should().existsByName(name);
      then(clothesAttributeDefRepository).should().save(any(ClothesAttributeDef.class));
    }

    @Test
    @DisplayName("이름 중복")
    void create_duplicate_name() {

      // given
      String name = "사이즈";

      given(clothesAttributeDefRepository.existsByName(name)).willReturn(true);

      // when, then
      assertThrows(ClothesAttributeDefAlreadyExistsException.class,
        () -> clothesAttributeDefService.create(name));
    }
  }
}