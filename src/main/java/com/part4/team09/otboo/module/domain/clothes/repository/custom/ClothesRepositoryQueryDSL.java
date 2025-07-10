package com.part4.team09.otboo.module.domain.clothes.repository.custom;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.entity.QClothes;
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
public class ClothesRepositoryQueryDSL {

  private final JPAQueryFactory queryFactory;
  private final QClothes clothes = QClothes.clothes;

  public List<Clothes> findByCursor(String cursor, UUID idAfter, int limit,
      ClothesType typeEqual, UUID ownerId, String sortBy, SortDirection sortDirection) {

    BooleanBuilder where = new BooleanBuilder();

    where.and(clothes.ownerId.eq(ownerId));
    where.and(clothes.type.eq(typeEqual));

    if (cursor != null && idAfter != null) {

      if (sortBy.equals("createdAt")) {

        LocalDateTime createdAt = LocalDateTime.parse(cursor);

        if (sortDirection == SortDirection.ASCENDING) {

          where.and(
              clothes.createdAt.gt(createdAt)
                  .or(clothes.createdAt.eq(createdAt).and(clothes.id.gt(idAfter)))
          );
        } else {
          where.and(
              clothes.createdAt.lt(createdAt)
                  .or(clothes.createdAt.eq(createdAt).and(clothes.id.lt(idAfter)))
          );
        }
      }
    }

    OrderSpecifier<?> order = getOrderSpecifier(sortBy, sortDirection);

    return queryFactory
        .selectFrom(clothes)
        .where(where)
        .orderBy(order)
        .limit(limit + 1)
        .fetch();
  }

  private OrderSpecifier<?> getOrderSpecifier(String sortBy, SortDirection sortDirection) {
    if (sortBy.equals("name")) {
      return sortDirection.equals(SortDirection.ASCENDING)
          ? clothes.name.asc()
          : clothes.name.desc();
    } else {
      return sortDirection.equals(SortDirection.ASCENDING)
          ? clothes.createdAt.asc()
          : clothes.createdAt.desc();
    }
  }
}
