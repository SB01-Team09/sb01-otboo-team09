package com.part4.team09.otboo.module.domain.clothes.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import jakarta.persistence.EntityManager;
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
@EnableJpaRepositories(basePackageClasses = ClothesAttributeDefRepository.class)
class ClothesAttributeDefRepositoryTest {

  @Autowired
  private ClothesAttributeDefRepository clothesAttributeDefRepository;

  @Autowired
  private EntityManager entityManager;

  @Nested
  @DisplayName("속정 정의 이름 존재 확인")
  class ExistsByName {

    @Test
    @DisplayName("이름이 존재할 경우 - true 반환")
    void existsByName_true() {

      // given
      String name = "사이즈";
      ClothesAttributeDef def = ClothesAttributeDef.create(name);
      clothesAttributeDefRepository.save(def);
      entityManager.flush();
      entityManager.clear();

      // when
      boolean result = clothesAttributeDefRepository.existsByName(name);

      // then
      assertTrue(result);
    }

    @Test
    @DisplayName("이름이 존재하지 않을 경우 - false 반환")
    void existsByName_false() {

      // given
      String name = "사이즈";

      // when
      boolean result = clothesAttributeDefRepository.existsByName(name);

      // then
      assertFalse(result);
    }
  }
}