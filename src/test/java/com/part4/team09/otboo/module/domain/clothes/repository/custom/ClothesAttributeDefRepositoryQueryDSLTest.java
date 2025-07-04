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
import org.junit.jupiter.api.DisplayName;
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
  private ClothesAttributeDefRepositoryQueryDSL queryDSL;

  @Autowired
  private ClothesAttributeDefRepository defRepository;

  @Autowired
  private SelectableValueRepository valueRepository;

  @Test
  @DisplayName("키워드를 포함하는 의상 속성 정의 명과 속성 값 찾기")
  void find_def_ids_by_keyword() {
    // given
    ClothesAttributeDef def1 = defRepository.save(ClothesAttributeDef.create("사이즈"));
    ClothesAttributeDef def2 = defRepository.save(ClothesAttributeDef.create("색상"));
    valueRepository.save(SelectableValue.create(def1.getId(), "S"));
    valueRepository.save(SelectableValue.create(def1.getId(), "M"));
    valueRepository.save(SelectableValue.create(def2.getId(), "레드"));

    // when
    List<UUID> result = queryDSL.findDefIdsByKeyword("레드");

    // then
    assertEquals(result.get(0), def2.getId());
    assertNotEquals(result.get(0), def1.getId());
  }

  @Test
  @DisplayName("커서 기반 페이지네이션")
  void find_by_cursor() {
    // given
    ClothesAttributeDef def1 = defRepository.save(ClothesAttributeDef.create("A"));
    ClothesAttributeDef def2 = defRepository.save(ClothesAttributeDef.create("B"));
    ClothesAttributeDef def3 = defRepository.save(ClothesAttributeDef.create("C"));

    List<UUID> ids = List.of(def1.getId(), def2.getId(), def3.getId());
    ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(
        def1.getName(), def1.getId(), 2, "name", SortDirection.ASCENDING, null
    );

    List<ClothesAttributeDef> defs = List.of(def2, def3);

    // when
    List<ClothesAttributeDef> result = queryDSL.findByCursor(ids, request);

    // then
    assertEquals(result, defs);
  }
}