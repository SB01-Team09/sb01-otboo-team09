package com.part4.team09.otboo.module.domain.clothes.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.part4.team09.otboo.config.QueryDslConfig;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@EnableJpaAuditing
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
@EnableJpaRepositories(basePackageClasses = ClothesRepository.class)
class ClothesRepositoryTest {

  @Autowired
  private ClothesRepository clothesRepository;

  @Autowired
  private EntityManager entityManager;

  private UUID userId;
  private Clothes clothes1;
  private Clothes clothes2;
  private Clothes clothes3;
  private Clothes clothes4;

  @BeforeEach
  void setUp() {

    userId = UUID.randomUUID();

    clothes1 = Clothes.create(userId, "셔츠", ClothesType.TOP, null);
    clothes2 = Clothes.create(userId, "정장바지", ClothesType.BOTTOM, null);
    clothes3 = Clothes.create(userId, "티셔츠", ClothesType.TOP, null);
    clothes4 = Clothes.create(UUID.randomUUID(), "정장 셔츠", ClothesType.TOP, null);

    clothesRepository.saveAll(List.of(clothes1, clothes2, clothes3, clothes4));
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @DisplayName("의상 id 리스트로 의상 개수 조회")
  void count_by_id_in() {

    // given
    List<UUID> ids = List.of(clothes1.getId(), clothes2.getId(), clothes3.getId(), clothes4.getId());

    // when
    int result = clothesRepository.countByIdIn(ids);

    // then
    assertEquals(result, 4);
  }

  @Test
  @DisplayName("사용자 id와 옷 타입으로 의상 개수 조회")
  void count_by_id_and_type() {

    // given
    ClothesType type = ClothesType.TOP;

    // when
    int result = clothesRepository.countByOwnerIdAndType(userId, type);

    // when
    assertEquals(result, 2);
  }
}