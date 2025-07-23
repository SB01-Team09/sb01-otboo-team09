package com.part4.team09.otboo.module.domain.clothes.service.dev;

import static com.part4.team09.otboo.module.domain.clothes.entity.QClothes.clothes;
import static com.part4.team09.otboo.module.domain.clothes.entity.QClothesAttributeDef.clothesAttributeDef;
import static com.part4.team09.otboo.module.domain.clothes.entity.QSelectableValue.selectableValue;
import static com.part4.team09.otboo.module.domain.clothes.entity.QClothesAttribute.clothesAttribute;

import com.part4.team09.otboo.config.QueryDslConfig;
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
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@EnableJpaAuditing
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class ClothesRepositoryPerformanceTest {

  @Autowired
  private EntityManager em;

  @Autowired
  private ClothesRepository clothesRepository;

  @Autowired
  private ClothesAttributeDefRepository clothesAttributeDefRepository;

  @Autowired
  private SelectableValueRepository selectableValueRepository;

  @Autowired
  private ClothesAttributeRepository clothesAttributeRepository;

  @Autowired
  private JPAQueryFactory queryFactory;

  @BeforeEach
  void setUp() {
    // 대량 데이터 생성
    List<ClothesAttributeDef> defs = IntStream.range(0, 10)
        .mapToObj(i -> ClothesAttributeDef.create("속성" + i))
        .map(clothesAttributeDefRepository::save)
        .toList();

    // 2. 각 속성 정의당 선택값 5개씩 생성
    Map<UUID, List<SelectableValue>> defIdToSelectableValues = new HashMap<>();
    for (ClothesAttributeDef def : defs) {
      List<SelectableValue> values = IntStream.range(0, 5)
          .mapToObj(i -> SelectableValue.create(def.getId(), "값" + i))
          .map(selectableValueRepository::save)
          .toList();
      defIdToSelectableValues.put(def.getId(), values);
    }

    // 3. 의상 1만 개 생성
    for (int i = 0; i < 10_000; i++) {
      UUID ownerId = UUID.randomUUID();
      Clothes clothes = Clothes.create(ownerId, "옷" + i, ClothesType.TOP, "img" + i + ".jpg");
      Clothes saved = clothesRepository.save(clothes);

      // 4. 각 의상에 대해 3개 속성을 랜덤하게 연결
      List<ClothesAttributeDef> def11 = new ArrayList<>(clothesAttributeDefRepository.findAll());
      Collections.shuffle(def11);
      for (int j = 0; j < 3; j++) {
        ClothesAttributeDef def = def11.get(j);
        List<SelectableValue> values = defIdToSelectableValues.get(def.getId());
        SelectableValue selected = values.get(new Random().nextInt(values.size()));

        clothesAttributeRepository.save(
            ClothesAttribute.create(saved.getId(), selected.getId())
        );
      }

      // 성능 최적화를 위해 주기적으로 flush/clear
      if (i % 1000 == 0) {
        clothesRepository.flush();
      }
    }
  }

  @Test
  void findById_vs_existsById_performance_test() {
    UUID id = clothesRepository.findAll().get(5000).getId();

    // existsById 측정
    long start1 = System.nanoTime();
    boolean exists = clothesRepository.existsById(id);
    long end1 = System.nanoTime();
    System.out.println("existsById: " + (end1 - start1) / 1_000_000.0 + " ms");

    // findById 측정
    long start2 = System.nanoTime();
    clothesRepository.findById(id);
    long end2 = System.nanoTime();
    System.out.println("findById: " + (end2 - start2) / 1_000_000.0 + " ms");
  }

  @Test
  void selectClothes_withIndex_vs_withoutIndex() {
    UUID testId = clothesRepository.findAll().get(0).getId(); // 첫 번째 ID 가져오기

    // 1. 인덱스 있음 (clothes.id 기준)
    long withIndexStart = System.currentTimeMillis();
    List<Clothes> resultWithIndex = queryFactory
        .selectFrom(clothes)
        .where(clothes.id.eq(testId))
        .fetch();
    long withIndexDuration = System.currentTimeMillis() - withIndexStart;

    // 2. 인덱스 없는 조건 (예: name, 가정상 비인덱스)
    long withoutIndexStart = System.currentTimeMillis();
    List<Clothes> resultWithoutIndex = queryFactory
        .selectFrom(clothes)
        .where(clothes.name.eq("옷0")) // name에 인덱스가 없다고 가정
        .fetch();
    long withoutIndexDuration = System.currentTimeMillis() - withoutIndexStart;

    System.out.printf("인덱스 있음 (id 기준): %d ms%n", withIndexDuration);
    System.out.printf("인덱스 없음 (name 기준): %d ms%n", withoutIndexDuration);
  }

  public void createIndex() {
    em.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_clothes_attribute_clothes_id ON clothes_attributes (clothes_id)")
        .executeUpdate();
  }

  public void dropIndex() {
    em.createNativeQuery("DROP INDEX IF EXISTS idx_clothes_attribute_clothes_id")
        .executeUpdate();
  }

  public List<ClothesAttributeRowDto> runQuery() {
    return queryFactory
        .select(Projections.constructor(
            ClothesAttributeRowDto.class,
            clothes.id,
            clothes.createdAt,
            clothes.ownerId,
            clothes.name,
            clothes.imageUrl,
            clothes.type,
            clothesAttributeDef.id,
            clothesAttributeDef.name,
            selectableValue.item
        ))
        .from(clothes)
        .leftJoin(clothesAttribute).on(clothesAttribute.clothesId.eq(clothes.id))
        .leftJoin(selectableValue).on(selectableValue.id.eq(clothesAttribute.selectableValueId))
        .leftJoin(clothesAttributeDef).on(clothesAttributeDef.id.eq(selectableValue.attributeDefId))
        .fetch();
  }

  @Test
  public void testPerformance() {
    // 1) 인덱스 생성 후 실행
    createIndex();
    long startWithIndex = System.currentTimeMillis();
    List<ClothesAttributeRowDto> withIndexResult = runQuery();
    long timeWithIndex = System.currentTimeMillis() - startWithIndex;

    // 2) 인덱스 삭제 후 실행
    dropIndex();
    long startWithoutIndex = System.currentTimeMillis();
    List<ClothesAttributeRowDto> withoutIndexResult = runQuery();
    long timeWithoutIndex = System.currentTimeMillis() - startWithoutIndex;

    System.out.println("인덱스 있을 때: " + timeWithIndex + " ms");
    System.out.println("인덱스 없을 때: " + timeWithoutIndex + " ms");
  }
}
