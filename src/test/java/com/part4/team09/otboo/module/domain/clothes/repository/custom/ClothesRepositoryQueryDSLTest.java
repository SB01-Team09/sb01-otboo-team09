package com.part4.team09.otboo.module.domain.clothes.repository.custom;

import static org.junit.jupiter.api.Assertions.*;

import com.part4.team09.otboo.config.QueryDslConfig;
import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
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
@Import({QueryDslConfig.class, ClothesRepositoryQueryDSL.class})
@ActiveProfiles("test")
class ClothesRepositoryQueryDSLTest {

  @Autowired
  private ClothesRepositoryQueryDSL clothesRepositoryQueryDSL;

  @Autowired
  private ClothesRepository clothesRepository;

  private UUID userId;
  private Clothes clothes1;
  private Clothes clothes2;
  private Clothes clothes3;

  @BeforeEach
  void setUp() throws InterruptedException {

    userId = UUID.randomUUID();

    clothes1 = Clothes.create(userId, "검정바지", ClothesType.BOTTOM, null);
    clothes2 = Clothes.create(userId, "스포츠 바지", ClothesType.BOTTOM, null);
    clothes3 = Clothes.create(userId, "반바지", ClothesType.BOTTOM, null);

    clothesRepository.save(clothes1);
    Thread.sleep(1000);
    clothesRepository.save(clothes2);
    Thread.sleep(1000);
    clothesRepository.save(clothes3);
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

      List<Clothes> clothes = List.of(clothes2, clothes3);

      // when
      List<Clothes> result = clothesRepositoryQueryDSL.findByCursor(cursor, idAfter, limit, typeEqual,
          ownerId, sortBy, sortDirection);

      // then
      assertEquals(result, clothes);
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

      List<Clothes> clothes = List.of(clothes1);

      // when
      List<Clothes> result = clothesRepositoryQueryDSL.findByCursor(cursor, idAfter, limit, typeEqual,
          ownerId, sortBy, sortDirection);

      // then
      assertEquals(result, clothes);
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

      List<Clothes> clothes = List.of(clothes3, clothes2);

      // when
      List<Clothes> result = clothesRepositoryQueryDSL.findByCursor(cursor, idAfter, limit, typeEqual,
          ownerId, sortBy, sortDirection);

      // then
      assertEquals(result, clothes);
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

      List<Clothes> clothes = List.of(clothes3, clothes1);

      // when
      List<Clothes> result = clothesRepositoryQueryDSL.findByCursor(cursor, idAfter, limit, typeEqual,
          ownerId, sortBy, sortDirection);

      // then
      assertEquals(result, clothes);
    }

    @Test
    @DisplayName("커서가 없을 경우")
    void find_by_cursor_success_without_cursor() {

      // given
      String cursor = null;
      UUID idAfter = null;
      int limit = 10;
      ClothesType typeEqual = ClothesType.BOTTOM;
      UUID ownerId = userId;
      String sortBy = "name";
      SortDirection sortDirection = SortDirection.ASCENDING;

      List<Clothes> clothes = List.of(clothes1, clothes3, clothes2);

      // when
      List<Clothes> result = clothesRepositoryQueryDSL.findByCursor(cursor, idAfter, limit, typeEqual,
          ownerId, sortBy, sortDirection);

      // then
      assertEquals(result, clothes);
    }
  }
}