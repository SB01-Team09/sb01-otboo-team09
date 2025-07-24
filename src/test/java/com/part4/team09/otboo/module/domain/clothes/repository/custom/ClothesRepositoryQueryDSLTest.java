package com.part4.team09.otboo.module.domain.clothes.repository.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.part4.team09.otboo.config.QueryDslConfig;
import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeRowDto;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
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
@Import({QueryDslConfig.class})
@ActiveProfiles("test")
class ClothesRepositoryQueryDSLTest {

  @Autowired
  private ClothesRepository clothesRepository;

  @Autowired
  private ClothesAttributeDefRepository clothesAttributeDefRepository;

  @Autowired
  private SelectableValueRepository selectableValueRepository;

  @Autowired
  private ClothesAttributeRepository clothesAttributeRepository;

  private UUID userId;
  private Clothes clothes1;
  private Clothes clothes2;
  private Clothes clothes3;
  private SelectableValue def1Value1;
  private SelectableValue def1Value2;
  private SelectableValue def2Value3;
  private SelectableValue def2Value4;

  @BeforeEach
  void setUp() throws InterruptedException {

    ClothesAttributeDef def1 = ClothesAttributeDef.create("사이즈");
    ClothesAttributeDef def2 = ClothesAttributeDef.create("색상");
    clothesAttributeDefRepository.save(def1);
    clothesAttributeDefRepository.save(def2);

    def1Value1 = SelectableValue.create(def1.getId(), "S");
    def1Value2 = SelectableValue.create(def1.getId(), "M");
    def2Value3 = SelectableValue.create(def2.getId(), "레드");
    def2Value4 = SelectableValue.create(def2.getId(), "블루");
    selectableValueRepository.saveAll(List.of(def1Value1, def1Value2, def2Value3, def2Value4));

    userId = UUID.randomUUID();

    clothes1 = Clothes.create(userId, "검정바지", ClothesType.BOTTOM, null);
    clothes2 = Clothes.create(userId, "스포츠 바지", ClothesType.BOTTOM, null);
    clothes3 = Clothes.create(userId, "반바지", ClothesType.BOTTOM, null);

    clothesRepository.save(clothes1);
    Thread.sleep(1000);
    clothesRepository.save(clothes2);
    Thread.sleep(1000);
    clothesRepository.save(clothes3);

    ClothesAttribute clothes1Attribute1 = ClothesAttribute.create(clothes1.getId(),
      def1Value1.getId());
    ClothesAttribute clothes1Attribute3 = ClothesAttribute.create(clothes1.getId(),
      def2Value3.getId());

    ClothesAttribute clothes2Attribute1 = ClothesAttribute.create(clothes2.getId(),
      def1Value1.getId());
    ClothesAttribute clothes2Attribute4 = ClothesAttribute.create(clothes2.getId(),
      def2Value4.getId());

    ClothesAttribute clothes3Attribute2 = ClothesAttribute.create(clothes3.getId(),
      def1Value2.getId());
    ClothesAttribute clothes3Attribute4 = ClothesAttribute.create(clothes3.getId(),
      def2Value4.getId());
    clothesAttributeRepository.saveAll(List.of(clothes1Attribute1, clothes1Attribute3,
      clothes2Attribute1,
      clothes2Attribute4, clothes3Attribute2, clothes3Attribute4));
  }

  @Nested
  @DisplayName("커서 기반 페이지네이션")
  class FindByCursor {

    @Test
    @DisplayName("생성일 오름차순")
    void find_by_cursor_success_created_at_asc() {

      // given
      String cursor = clothes1.getCreatedAt().plusNanos(500000000).toString();
      UUID idAfter = clothes1.getId();
      int limit = 10;
      ClothesType typeEqual = ClothesType.BOTTOM;
      UUID ownerId = userId;
      String sortBy = "createdAt";
      SortDirection sortDirection = SortDirection.ASCENDING;


      // when
      List<ClothesAttributeRowDto> result = clothesRepository.findByCursor(cursor, idAfter, limit,
        typeEqual, ownerId, sortBy, sortDirection);

      // then
      assertNotNull(result);
      assertEquals(clothes2.getId(), result.get(0).clothesId());
      assertEquals(def1Value1.getItem(), result.get(0).selectableValueItem());

      assertEquals(clothes2.getId(), result.get(1).clothesId());
      assertEquals(def2Value4.getItem(), result.get(1).selectableValueItem());

      assertEquals(clothes3.getId(), result.get(2).clothesId());
      assertEquals(def1Value2.getItem(), result.get(2).selectableValueItem());

      assertEquals(clothes3.getId(), result.get(3).clothesId());
      assertEquals(def2Value4.getItem(), result.get(3).selectableValueItem());
    }

    @Test
    @DisplayName("생성일 내림차순")
    void find_by_cursor_success_created_at_desc() {

      // given
      String cursor = clothes2.getCreatedAt().minusNanos(500000000).toString();
      UUID idAfter = clothes2.getId();
      int limit = 10;
      ClothesType typeEqual = ClothesType.BOTTOM;
      UUID ownerId = userId;
      String sortBy = "createdAt";
      SortDirection sortDirection = SortDirection.DESCENDING;

      // when
      List<ClothesAttributeRowDto> result = clothesRepository.findByCursor(cursor, idAfter, limit,
        typeEqual, ownerId, sortBy, sortDirection);

      // then
      assertNotNull(result);
      assertEquals(clothes1.getId(), result.get(0).clothesId());
      assertEquals(def1Value1.getItem(), result.get(0).selectableValueItem());

      assertEquals(clothes1.getId(), result.get(1).clothesId());
      assertEquals(def2Value3.getItem(), result.get(1).selectableValueItem());
    }

    @Test
    @DisplayName("이름 오름차순")
    void find_by_cursor_success_name_asc() {

      // given
      String cursor = clothes1.getName();
      UUID idAfter = clothes1.getId();
      int limit = 10;
      ClothesType typeEqual = ClothesType.BOTTOM;
      UUID ownerId = userId;
      String sortBy = "name";
      SortDirection sortDirection = SortDirection.ASCENDING;

      // when
      List<ClothesAttributeRowDto> result = clothesRepository.findByCursor(cursor, idAfter, limit,
        typeEqual, ownerId, sortBy, sortDirection);

      // then
      assertNotNull(result);
      assertEquals(clothes3.getId(), result.get(0).clothesId());
      assertEquals(def1Value2.getItem(), result.get(0).selectableValueItem());

      assertEquals(clothes3.getId(), result.get(1).clothesId());
      assertEquals(def2Value4.getItem(), result.get(1).selectableValueItem());

      assertEquals(clothes2.getId(), result.get(2).clothesId());
      assertEquals(def1Value1.getItem(), result.get(2).selectableValueItem());

      assertEquals(clothes2.getId(), result.get(3).clothesId());
      assertEquals(def2Value4.getItem(), result.get(3).selectableValueItem());
    }

    @Test
    @DisplayName("이름 내림차순")
    void find_by_cursor_success_name_desc() {

      // given
      String cursor = clothes2.getName();
      UUID idAfter = clothes2.getId();
      int limit = 10;
      ClothesType typeEqual = ClothesType.BOTTOM;
      UUID ownerId = userId;
      String sortBy = "name";
      SortDirection sortDirection = SortDirection.DESCENDING;

      // when
      List<ClothesAttributeRowDto> result = clothesRepository.findByCursor(cursor, idAfter, limit,
        typeEqual, ownerId, sortBy, sortDirection);

      // then
      assertNotNull(result);
      assertEquals(clothes3.getId(), result.get(0).clothesId());
      assertEquals(def1Value2.getItem(), result.get(0).selectableValueItem());

      assertEquals(clothes3.getId(), result.get(1).clothesId());
      assertEquals(def2Value4.getItem(), result.get(1).selectableValueItem());

      assertEquals(clothes1.getId(), result.get(2).clothesId());
      assertEquals(def1Value1.getItem(), result.get(2).selectableValueItem());

      assertEquals(clothes1.getId(), result.get(3).clothesId());
      assertEquals(def2Value3.getItem(), result.get(3).selectableValueItem());
    }

    @Test
    @DisplayName("커서가 없을 경우")
    void find_by_cursor_success_without_cursor_and_id_after() {

      // given
      int limit = 10;
      ClothesType typeEqual = ClothesType.BOTTOM;
      UUID ownerId = userId;
      String sortBy = "name";
      SortDirection sortDirection = SortDirection.ASCENDING;

      // when
      List<ClothesAttributeRowDto> result = clothesRepository.findByCursor(null, null, limit,
        typeEqual, ownerId, sortBy, sortDirection);

      // then
      assertNotNull(result);
      assertEquals(clothes1.getId(), result.get(0).clothesId());
      assertEquals(def1Value1.getItem(), result.get(0).selectableValueItem());

      assertEquals(clothes1.getId(), result.get(1).clothesId());
      assertEquals(def2Value3.getItem(), result.get(1).selectableValueItem());

      assertEquals(clothes3.getId(), result.get(2).clothesId());
      assertEquals(def1Value2.getItem(), result.get(2).selectableValueItem());

      assertEquals(clothes3.getId(), result.get(3).clothesId());
      assertEquals(def2Value4.getItem(), result.get(3).selectableValueItem());

      assertEquals(clothes2.getId(), result.get(4).clothesId());
      assertEquals(def1Value1.getItem(), result.get(4).selectableValueItem());

      assertEquals(clothes2.getId(), result.get(5).clothesId());
      assertEquals(def2Value4.getItem(), result.get(5).selectableValueItem());
    }
    @Test
    @DisplayName("idAfter가 없을 경우")
    void find_by_cursor_success_without_id_after() {

      // given
      int limit = 10;
      String cursor = clothes2.getName();
      ClothesType typeEqual = ClothesType.BOTTOM;
      UUID ownerId = userId;
      String sortBy = "name";
      SortDirection sortDirection = SortDirection.ASCENDING;

      // when
      List<ClothesAttributeRowDto> result = clothesRepository.findByCursor(cursor, null, limit,
          typeEqual, ownerId, sortBy, sortDirection);

      // then
      assertNotNull(result);
      assertEquals(clothes1.getId(), result.get(0).clothesId());
      assertEquals(def1Value1.getItem(), result.get(0).selectableValueItem());

      assertEquals(clothes1.getId(), result.get(1).clothesId());
      assertEquals(def2Value3.getItem(), result.get(1).selectableValueItem());

      assertEquals(clothes3.getId(), result.get(2).clothesId());
      assertEquals(def1Value2.getItem(), result.get(2).selectableValueItem());

      assertEquals(clothes3.getId(), result.get(3).clothesId());
      assertEquals(def2Value4.getItem(), result.get(3).selectableValueItem());

      assertEquals(clothes2.getId(), result.get(4).clothesId());
      assertEquals(def1Value1.getItem(), result.get(4).selectableValueItem());

      assertEquals(clothes2.getId(), result.get(5).clothesId());
      assertEquals(def2Value4.getItem(), result.get(5).selectableValueItem());
    }

    @Test
    @DisplayName("커서가 없을 경우")
    void find_by_cursor_success_without_cursor() {

      // given
      int limit = 10;
      UUID idAfter = clothes2.getId();
      ClothesType typeEqual = ClothesType.BOTTOM;
      UUID ownerId = userId;
      String sortBy = "name";
      SortDirection sortDirection = SortDirection.ASCENDING;

      // when
      List<ClothesAttributeRowDto> result = clothesRepository.findByCursor(null, idAfter, limit,
          typeEqual, ownerId, sortBy, sortDirection);

      // then
      assertNotNull(result);
      assertEquals(clothes1.getId(), result.get(0).clothesId());
      assertEquals(def1Value1.getItem(), result.get(0).selectableValueItem());

      assertEquals(clothes1.getId(), result.get(1).clothesId());
      assertEquals(def2Value3.getItem(), result.get(1).selectableValueItem());

      assertEquals(clothes3.getId(), result.get(2).clothesId());
      assertEquals(def1Value2.getItem(), result.get(2).selectableValueItem());

      assertEquals(clothes3.getId(), result.get(3).clothesId());
      assertEquals(def2Value4.getItem(), result.get(3).selectableValueItem());

      assertEquals(clothes2.getId(), result.get(4).clothesId());
      assertEquals(def1Value1.getItem(), result.get(4).selectableValueItem());

      assertEquals(clothes2.getId(), result.get(5).clothesId());
      assertEquals(def2Value4.getItem(), result.get(5).selectableValueItem());
    }

    @Test
    @DisplayName("해당 의상이 없을 경우 빈 리스트 반환")
    void find_by_cursor_success_not_found_clothes() {

      // given
      int limit = 10;
      ClothesType typeEqual = ClothesType.BOTTOM;
      UUID ownerId = UUID.randomUUID();
      String sortBy = "name";
      SortDirection sortDirection = SortDirection.ASCENDING;

      // when
      List<ClothesAttributeRowDto> result = clothesRepository.findByCursor(null, null, limit,
          typeEqual, ownerId, sortBy, sortDirection);

      // then
      assertEquals(List.of(), result);
    }
  }

  @Nested
  @DisplayName("의상 id로 조회")
  class FindByClothesId {

    @Test
    @DisplayName("의상 조회 성공")
    void find_by_clothes_id_success() {

      // given
      UUID clothesId = clothes1.getId();

      // when
      List<ClothesAttributeRowDto> result = clothesRepository.findByClothesId(clothesId);

      // then
      assertNotNull(result);
      assertEquals(clothesId, result.get(0).clothesId());
      assertEquals(def1Value1.getItem(), result.get(0).selectableValueItem());

      assertEquals(clothesId, result.get(1).clothesId());
      assertEquals(def2Value3.getItem(), result.get(1).selectableValueItem());
    }
  }
}