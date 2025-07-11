package com.part4.team09.otboo.module.domain.clothes.repository.custom;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.QClothes;
import com.part4.team09.otboo.module.domain.clothes.entity.QClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.entity.QClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.QSelectableValue;
import com.part4.team09.otboo.module.domain.recommendation.dto.ClothingOption;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClothesRepositoryQueryDSL {

  private final JPAQueryFactory queryFactory;

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

}
