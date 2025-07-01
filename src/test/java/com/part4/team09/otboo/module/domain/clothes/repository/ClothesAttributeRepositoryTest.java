package com.part4.team09.otboo.module.domain.clothes.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
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

  @Nested
  @DisplayName("속성 값 id로 삭제")
  class DeleteBySelectableValueIdIn {

    @Test
    @DisplayName("삭제 성공")
    void delete_by_selectable_value_id_in() {

      // given
      UUID clothesId = UUID.randomUUID();
      UUID selectableValueId = UUID.randomUUID();

      ClothesAttribute clothesAttribute = ClothesAttribute.create(clothesId, selectableValueId);

      clothesAttributeRepository.save(clothesAttribute);
      entityManager.flush();
      entityManager.clear();

      // when
      clothesAttributeRepository.deleteBySelectableValueIdIn(List.of(selectableValueId));
      List<ClothesAttribute> result = clothesAttributeRepository.findAll();

      // then
      assertTrue(result.isEmpty());
    }
  }
}