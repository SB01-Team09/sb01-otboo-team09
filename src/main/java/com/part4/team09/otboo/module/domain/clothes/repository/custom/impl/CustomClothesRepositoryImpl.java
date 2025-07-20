package com.part4.team09.otboo.module.domain.clothes.repository.custom.impl;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesWithAttributesDto;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.entity.QClothes;
import com.part4.team09.otboo.module.domain.clothes.entity.QClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.entity.QClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.QSelectableValue;
import com.part4.team09.otboo.module.domain.clothes.repository.custom.CustomClothesRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
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
  public List<ClothesWithAttributesDto> findByCursor(String cursor, UUID idAfter, int limit,
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
        ClothesWithAttributesDto.class,
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
  public List<ClothesWithAttributesDto> findByClothesId(UUID clothesId) {
    return queryFactory
      .select(Projections.constructor(
        ClothesWithAttributesDto.class,
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
