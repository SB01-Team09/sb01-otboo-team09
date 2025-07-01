package com.part4.team09.otboo.module.domain.clothes.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
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
  @DisplayName("속성 정의 id로 속성 값들 찾기")
  class FindAllByAttributeDefId {

    @Test
    @DisplayName("찾기 성공")
    void find_all_by_attribute_def_id() {
      // given
      UUID defId = UUID.randomUUID();
      List<SelectableValue> selectableValues = Stream.of("S", "M", "L")
          .map(value -> SelectableValue.create(defId, value))
          .toList();

      selectableValueRepository.saveAll(selectableValues);
      entityManager.flush();
      entityManager.clear();

      // given
      List<SelectableValue> results = selectableValueRepository.findAllByAttributeDefId(defId);

      // then
      assertEquals(
          results.stream().map(SelectableValue::getItem).toList(),
          selectableValues.stream().map(SelectableValue::getItem).toList()
      );
    }
  }

  @Nested
  @DisplayName("정의 id 리스트로 속성 값 리스트 반환")
  class FindAllByAttributeDefIdIn {

    @Test
    @DisplayName("성공")
    void find_all_by_attribute_def_id_in_success() {

      // given
      UUID defId1 = UUID.randomUUID();
      List<SelectableValue> selectableValues1 = Stream.of("S", "M", "L")
          .map(value -> SelectableValue.create(defId1, value))
          .toList();
      UUID defId2 = UUID.randomUUID();
      List<SelectableValue> selectableValues2 = Stream.of("없음", "있음", "조금 있음")
          .map(value -> SelectableValue.create(defId2, value))
          .toList();

      List<UUID> defIds = List.of(defId1, defId2);

      selectableValueRepository.saveAll(selectableValues1);
      selectableValueRepository.saveAll(selectableValues2);
      entityManager.flush();
      entityManager.clear();

      List<SelectableValue> selectableValues = selectableValueRepository.findAll();

      // when
      List<SelectableValue> results = selectableValueRepository.findAllByAttributeDefIdIn(defIds);

      // then
      assertEquals(results, selectableValues);
    }
  }

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
      assertTrue(result.isEmpty());
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

  @Nested
  @DisplayName("아이디 리스트로 삭제")
  class DeleteByIdIn {

    @Test
    @DisplayName("리스트로 삭제 성공")
    void delete_by_id_in() {

      // given
      UUID id = UUID.randomUUID();
      SelectableValue selectableValue = SelectableValue.create(id, "S");

      selectableValueRepository.save(selectableValue);
      entityManager.flush();
      entityManager.clear();

      // when
      selectableValueRepository.deleteByIdIn(List.of(selectableValue.getId()));
      List<SelectableValue> result = selectableValueRepository.findAll();

      // then
      assertTrue(result.isEmpty());
    }
  }
}