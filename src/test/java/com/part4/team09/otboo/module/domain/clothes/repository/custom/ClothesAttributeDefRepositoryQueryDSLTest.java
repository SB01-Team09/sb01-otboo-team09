package com.part4.team09.otboo.module.domain.clothes.repository.custom;

import static org.junit.jupiter.api.Assertions.*;

import com.part4.team09.otboo.config.QueryDslConfig;
import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefFindRequest;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.SelectableValueRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@EnableJpaAuditing
@Import({QueryDslConfig.class, ClothesAttributeDefRepositoryQueryDSL.class})
@ActiveProfiles("test")
class ClothesAttributeDefRepositoryQueryDSLTest {

  @Autowired
  private ClothesAttributeDefRepositoryQueryDSL clothesAttributeDefRepositoryQueryDSL;

  @Autowired
  private ClothesAttributeDefRepository clothesAttributeDefRepository;

  @Autowired
  private SelectableValueRepository selectableValueRepository;

  private ClothesAttributeDef def1;
  private ClothesAttributeDef def2;
  private ClothesAttributeDef def3;

  @BeforeEach
  void setUp() throws InterruptedException {
    def1 = clothesAttributeDefRepository.save(ClothesAttributeDef.create("사이즈"));
    Thread.sleep(1000);
    def2 = clothesAttributeDefRepository.save(ClothesAttributeDef.create("색상"));
    Thread.sleep(1000);
    def3 = clothesAttributeDefRepository.save(ClothesAttributeDef.create("신축성"));
  }

  @Test
  @DisplayName("키워드를 포함하는 의상 속성 정의 명과 속성 값 찾기")
  void find_def_ids_by_keyword() {
    // given
    selectableValueRepository.save(SelectableValue.create(def1.getId(), "S"));
    selectableValueRepository.save(SelectableValue.create(def1.getId(), "M"));
    selectableValueRepository.save(SelectableValue.create(def2.getId(), "레드"));

    // when
    List<UUID> result = clothesAttributeDefRepositoryQueryDSL.findDefIdsByKeyword("레드");

    // then
    assertEquals(result.get(0), def2.getId());
    assertNotEquals(result.get(0), def1.getId());
  }

  @Nested
  @DisplayName("커서 기반 페이지네이션")
  class FindByCursor {

    @Test
    @DisplayName("커서 기반 조회 - 이름 오름차순")
    void find_by_cursor_success_order_by_name_asc() {

      // given
      List<UUID> ids = List.of(def1.getId(), def2.getId(), def3.getId());
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(
          def1.getName(), def1.getId(), 2, "name", SortDirection.ASCENDING, null
      );

      List<ClothesAttributeDef> defs = List.of(def2, def3);

      // when
      List<ClothesAttributeDef> result = clothesAttributeDefRepositoryQueryDSL.findByCursor(ids, request);

      // then
      assertEquals(result, defs);

    }

    @Test
    @DisplayName("커서 기반 조회 - 이름 내림차순")
    void find_by_cursor_success_order_by_name_desc() {

      // given
      List<UUID> ids = List.of(def1.getId(), def2.getId(), def3.getId());
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(
          def2.getName(), def2.getId(), 1, "name", SortDirection.DESCENDING, null
      );

      List<ClothesAttributeDef> defs = List.of(def1);

      // when
      List<ClothesAttributeDef> result = clothesAttributeDefRepositoryQueryDSL.findByCursor(ids, request);

      // then
      assertEquals(result, defs);

    }

    @Test
    @DisplayName("커서 기반 조회 - 생성일 오름차순")
    void find_by_cursor_success_order_by_created_at_asc() {

      // given
      List<UUID> ids = List.of(def1.getId(), def2.getId(), def3.getId());
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(
          def1.getCreatedAt().plusNanos(1).toString(), def1.getId(), 2, "createdAt", SortDirection.ASCENDING, null
      );

      List<ClothesAttributeDef> defs = List.of(def2, def3);

      // when
      List<ClothesAttributeDef> result = clothesAttributeDefRepositoryQueryDSL.findByCursor(ids, request);

      // then
      assertEquals(result, defs);

    }

    @Test
    @DisplayName("커서 기반 조회 - 생성일 내림차순")
    void find_by_cursor_success_order_by_created_at_desc() {

      // given
      List<UUID> ids = List.of(def1.getId(), def2.getId(), def3.getId());
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(
          def2.getCreatedAt().plusNanos(1).toString(), def2.getId(), 1, "createdAt", SortDirection.DESCENDING, null
      );

      List<ClothesAttributeDef> defs = List.of(def1);

      // when
      List<ClothesAttributeDef> result = clothesAttributeDefRepositoryQueryDSL.findByCursor(ids, request);

      // then
      assertEquals(result, defs);

    }

    @Test
    @DisplayName("커서 없이 조회 - 이름 오름차순")
    void find_by_cursor_success_order_by_name_at_asc() {

      // given
      List<UUID> ids = List.of(def1.getId(), def2.getId(), def3.getId());
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(
          null, null, 1, "name", SortDirection.ASCENDING, null
      );

      List<ClothesAttributeDef> defs = List.of(def1, def2);

      // when
      List<ClothesAttributeDef> result = clothesAttributeDefRepositoryQueryDSL.findByCursor(ids, request);

      // then
      assertEquals(result, defs);

    }
  }
}