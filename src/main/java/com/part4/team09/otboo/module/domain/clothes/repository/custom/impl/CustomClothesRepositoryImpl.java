package com.part4.team09.otboo.module.domain.clothes.repository.custom.impl;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeRowDto;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.entity.QClothes;
import com.part4.team09.otboo.module.domain.clothes.entity.QClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.entity.QClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.QSelectableValue;
import com.part4.team09.otboo.module.domain.clothes.repository.custom.CustomClothesRepository;
import com.part4.team09.otboo.module.domain.recommendation.dto.ClothingOption;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomClothesRepositoryImpl implements CustomClothesRepository {

  private final JPAQueryFactory queryFactory;
  private final QClothes clothes = QClothes.clothes;
  private final QClothesAttribute clothesAttribute = QClothesAttribute.clothesAttribute;
  private final QSelectableValue selectableValue = QSelectableValue.selectableValue;
  private final QClothesAttributeDef clothesAttributeDef = QClothesAttributeDef.clothesAttributeDef;

  @Override
  public List<ClothesAttributeRowDto> findByCursor(String cursor, UUID idAfter, int limit,
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
      } else {

        if (sortDirection == SortDirection.ASCENDING) {

          where.and(
            clothes.name.gt(cursor)
              .or(clothes.name.eq(cursor).and(clothes.id.gt(idAfter)))
          );
        } else {
          where.and(
            clothes.name.lt(cursor)
              .or(clothes.name.eq(cursor).and(clothes.id.lt(idAfter)))
          );
        }
      }
    }

    OrderSpecifier<?> order = getOrderSpecifier(sortBy, sortDirection);

    List<Clothes> clothesList = queryFactory
      .selectFrom(clothes)
      .where(where)
      .orderBy(order)
      .limit(limit + 1)
      .fetch();

    if (clothesList.isEmpty()) {
      return List.of();
    }

    List<UUID> clothesIds = clothesList.stream()
      .map(Clothes::getId)
      .toList();

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
      .where(clothes.id.in(clothesIds))
      .orderBy(order)
      .fetch();
  }

  @Override
  public List<ClothesAttributeRowDto> findByClothesId(UUID clothesId) {
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
      .where(clothes.id.eq(clothesId))
      .fetch();
  }

  @Override
  public List<Clothes> findAllOrderedByAttributeScores(List<ClothingOption> options, int limit) {
    QClothes c = QClothes.clothes;
    QClothesAttribute ca = QClothesAttribute.clothesAttribute;
    QSelectableValue sv = QSelectableValue.selectableValue;
    QClothesAttributeDef cad = QClothesAttributeDef.clothesAttributeDef;

    NumberExpression<Integer> totalScoreExpr = buildScoreExpression(options, cad.name, sv.item);

    return queryFactory
        .select(c)
        .from(c)
        .join(ca).on(ca.clothesId.eq(c.id))
        .join(sv).on(sv.id.eq(ca.selectableValueId))
        .join(cad).on(cad.id.eq(sv.attributeDefId))
        .groupBy(c.id)
        .orderBy(totalScoreExpr.asc())
        .limit(limit)
        .fetch();
  }

  private NumberExpression<Integer> buildScoreExpression(
      List<ClothingOption> options,
      StringExpression attributeName,
      StringExpression itemValue) {
    CaseBuilder caseBuilder = new CaseBuilder();
    CaseBuilder.Cases<Integer, NumberExpression<Integer>> cases = null;

    for (ClothingOption option : options) {
      String attr = option.attribute();
      List<String> values = option.values();

      for (int i = 0; i < values.size(); i++) {
        String value = values.get(i);

        NumberExpression<Integer> scoreExpr =
            Expressions.numberTemplate(Integer.class, "{0}", i + 1);

        if (cases == null) {
          cases = caseBuilder
              .when(attributeName.eq(attr).and(itemValue.eq(value)))
              .then(scoreExpr);
        } else {
          cases = cases
              .when(attributeName.eq(attr).and(itemValue.eq(value)))
              .then(scoreExpr);
        }
      }
    }

    if (cases == null) {
      return Expressions.numberTemplate(Integer.class, "{0}", 999);
    }

    return Expressions.numberTemplate(
        Integer.class,
        "SUM({0})",
        cases.otherwise(999)
    );
  }

  private OrderSpecifier<?> getOrderSpecifier(String sortBy, SortDirection sortDirection) {
    if (sortBy.equals("createdAt")) {
      return sortDirection.equals(SortDirection.ASCENDING)
        ? clothes.createdAt.asc()
        : clothes.createdAt.desc();
    } else {
      return sortDirection.equals(SortDirection.ASCENDING)
        ? clothes.name.asc()
        : clothes.name.desc();
    }
  }
}
