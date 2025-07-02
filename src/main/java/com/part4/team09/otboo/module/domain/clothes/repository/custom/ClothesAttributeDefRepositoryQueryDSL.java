package com.part4.team09.otboo.module.domain.clothes.repository.custom;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefFindRequest;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.QClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.QSelectableValue;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ClothesAttributeDefRepositoryQueryDSL {

  private final JPAQueryFactory queryFactory;
  QClothesAttributeDef def = QClothesAttributeDef.clothesAttributeDef;
  QSelectableValue value = QSelectableValue.selectableValue;

  // 속성 정의, 속성 값에 키워드가 있을 경우의 defId 찾기
  public List<UUID> findDefIdsByKeyword(String keyword) {

    return queryFactory
        .select(def.id)
        .from(def)
        // 속성 값과 조인
        .leftJoin(value).on(value.attributeDefId.eq(def.id))
        .where(
            def.name.containsIgnoreCase(keyword)
                .or(value.item.containsIgnoreCase(keyword))
        )
        .distinct()
        .fetch();
  }

  public List<ClothesAttributeDef> findByCursor(List<UUID> defIds
      , ClothesAttributeDefFindRequest request) {

    BooleanBuilder where = new BooleanBuilder();

    where.and(def.id.in(defIds));

    if (request.cursor() != null && request.idAfter() != null) {
      if (request.sortBy().equals("name")) {
        if (request.sortDirection() == SortDirection.ASCENDING) {
          // 오름차순일 경우 큰 값
          where.and(
              def.name.gt(request.cursor())
                  .or(def.name.eq(request.cursor()).and(def.id.gt(request.idAfter())))
          );
        } else {
          // 내림차순일 경우 작은 값
          where.and(
              def.name.lt(request.cursor())
                  .or(def.name.eq(request.cursor()).and(def.id.lt(request.idAfter())))
          );
        }
      } else {
        LocalDateTime createdAt = LocalDateTime.parse(request.cursor());

        if (request.sortDirection() == SortDirection.ASCENDING) {
          where.and(
              def.createdAt.gt(createdAt)
                  .or(def.createdAt.eq(createdAt).and(def.id.gt(request.idAfter())))
          );
        } else {
          where.and(
              def.createdAt.lt(createdAt)
                  .or(def.createdAt.eq(createdAt).and(def.id.lt(request.idAfter())))
          );
        }
      }
    }


    OrderSpecifier<?> order = getOrderSpecifier(request);

    return queryFactory
        .selectFrom(def)
        .where(where)
        .orderBy(order)
        .limit(request.limit() + 1)
        .fetch();
  }


  private OrderSpecifier<?> getOrderSpecifier(ClothesAttributeDefFindRequest request) {
    if (request.sortBy().equals("name")) {
      return request.sortDirection().equals(SortDirection.ASCENDING)
          ? def.name.asc()
          : def.name.desc();
    } else {
      return request.sortDirection().equals(SortDirection.ASCENDING)
          ? def.createdAt.asc()
          : def.createdAt.desc();
    }
  }
}
