package com.part4.team09.otboo.module.domain.clothes.repository;

import static org.junit.jupiter.api.Assertions.*;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
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
@EnableJpaRepositories(basePackageClasses = SelectableValueRepository.class)
class SelectableValueRepositoryTest {

  @Autowired
  private SelectableValueRepository selectableValueRepository;

  @Autowired
  private EntityManager entityManager;

  @Nested
  @DisplayName("의상 속성 정의 id로 속성 값 전부 삭제")
  class DeleteAllByAttributeDefId {

    @Test
    @DisplayName("삭제 성공")
    void deleteAllByAttributeDefId_success() {

      // given
      UUID defId = UUID.randomUUID();
      List<String> values = List.of("S", "M", "L");

      List<SelectableValue> selectableValues = values.stream()
          .map(value -> SelectableValue.create(defId, value))
          .toList();

      selectableValueRepository.saveAll(selectableValues);
      entityManager.flush();
      entityManager.clear();

      // when
      selectableValueRepository.deleteAllByAttributeDefId(defId);
      List<SelectableValue> result = selectableValueRepository.findAll();

      // then
      assertEquals(result, List.of());
    }

    @Test
    @DisplayName("잘못된 id")
    void deleteAllByAttributeDefId_withInvalidId() {

      // given
      UUID invalidId = UUID.randomUUID();

      // when, then
      assertDoesNotThrow(() -> selectableValueRepository.deleteAllByAttributeDefId(invalidId));
    }
  }
}