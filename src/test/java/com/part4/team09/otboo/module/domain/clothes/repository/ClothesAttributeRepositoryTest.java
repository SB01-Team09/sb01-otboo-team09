package com.part4.team09.otboo.module.domain.clothes.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@EnableJpaAuditing
@ActiveProfiles("test")
@EnableJpaRepositories(basePackageClasses = ClothesAttributeRepository.class)
class ClothesAttributeRepositoryTest {

  @Autowired
  private ClothesAttributeRepository clothesAttributeRepository;

  @Autowired
  private EntityManager entityManager;

  private UUID clothesId;
  private ClothesAttribute clothesAttribute1;
  private ClothesAttribute clothesAttribute2;

  @BeforeEach
  void setUp() {

    clothesId = UUID.randomUUID();
    UUID selectableValueId1 = UUID.randomUUID();
    UUID selectableValueId2 = UUID.randomUUID();

    clothesAttribute1 = ClothesAttribute.create(clothesId, selectableValueId1);
    clothesAttribute2 = ClothesAttribute.create(clothesId, selectableValueId2);

    clothesAttributeRepository.saveAll(List.of(clothesAttribute1, clothesAttribute2));
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @DisplayName("의상 id로 의상 속성 연관 리스트 조회")
  void find_all_by_clothes_id() {

    // given, when
    List<ClothesAttribute> result = clothesAttributeRepository.findAllByClothesId(clothesId);

    // then
    assertEquals(result.get(0).getId(), clothesAttribute1.getId());
    assertEquals(result.get(1).getId(), clothesAttribute2.getId());
  }

  @Nested
  @DisplayName("속성 값 id로 삭제")
  class DeleteBySelectableValueIdIn {

    @Test
    @DisplayName("삭제 성공")
    void delete_by_selectable_value_id_in() {

      // given,  when
      clothesAttributeRepository.deleteBySelectableValueIdIn(List.of(clothesAttribute1.getSelectableValueId(),
          clothesAttribute2.getSelectableValueId()));
      List<ClothesAttribute> result = clothesAttributeRepository.findAll();

      // then
      assertTrue(result.isEmpty());
    }
  }

  @Test
  @DisplayName("의상 id로 연관 삭제")
  void delete_all_by_clothes_id() {

    // given, when
    clothesAttributeRepository.deleteAllByClothesId(clothesId);

    List<ClothesAttribute> result = clothesAttributeRepository.findAllByClothesId(clothesId);

    // then
    assertTrue(result.isEmpty());
  }
}