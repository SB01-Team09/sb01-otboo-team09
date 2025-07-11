package com.part4.team09.otboo.module.domain.feed.repository;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.entity.QFeed;
import com.part4.team09.otboo.module.domain.feed.entity.QOotd;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Repository
@RequiredArgsConstructor
public class FeedRepositoryQueryDSL {

    private final JPAQueryFactory queryFactory;

    QFeed feed = QFeed.feed;
    QOotd ootd = QOotd.ootd;


    // 피드 목록 조회
    public List<Feed> getFeeds(String cursor, UUID idAfter, int limit, String sortBy, SortDirection sortDirection, String keywordLike, Weather.SkyStatus skyStatusEqual, Precipitation.PrecipitationType precipitationTypeEqual, UUID authorIdEqual) {
        return queryFactory
                .selectFrom(feed)
                .join(ootd).on(feed.id.eq(ootd.feedId)) // OOTD 조인
                .where(
                        likeContent(keywordLike),
                        equalAuthorId(authorIdEqual),
                        cursorCondition(cursor, idAfter, sortBy, sortDirection)
                )
                .orderBy(getSortOrder(sortBy, sortDirection))
                .limit(limit)
                .fetch();
    }

    // 피드 개수
    public int countFeeds(String keywordLike, Weather.SkyStatus skyStatusEqual, Precipitation.PrecipitationType precipitationTypeEqual, UUID authorIdEqual){
        Long count = queryFactory
                .select(feed.count())
                .from(feed)
                .where(
                        likeContent(keywordLike),
                        equalAuthorId(authorIdEqual)
                )
                .fetchOne();
        return count != null ? Math.toIntExact(count) : 0;
    }


    private BooleanExpression likeContent(String keyword) {
        return keyword != null && !keyword.isBlank() ? feed.content.likeIgnoreCase("%" + keyword + "%") : null;
    }

    private BooleanExpression equalAuthorId(UUID authorId) {
        return authorId != null ? feed.authorId.eq(authorId) : null;
    }

    private BooleanExpression cursorCondition(String cursor, UUID idAfter, String sortBy, SortDirection sortDirection) {

        if (cursor == null) return null;

        if (sortBy.equals("createdAt")) {
            if (sortDirection.equals("DESCENDING")) {
                BooleanExpression condition = feed.createdAt.lt(LocalDateTime.parse(cursor));
                if (idAfter != null) {
                    condition = condition.or(
                            feed.createdAt.eq(LocalDateTime.parse(cursor))
                                    .and(feed.id.lt(idAfter))
                    );
                }
                return condition;
            } else if (sortDirection.equals("ASCENDING")) {
                BooleanExpression condition = feed.createdAt.gt(LocalDateTime.parse(cursor));
                if (idAfter != null) {
                    condition = condition.or(
                            feed.createdAt.eq(LocalDateTime.parse(cursor))
                                    .and(feed.id.gt(idAfter))
                    );
                }
                return condition;
            }
        }

        if (sortBy.equals("likes")) {
            if (sortDirection.equals("DESCENDING")) {
                BooleanExpression condition = feed.likeCount.lt(Integer.parseInt(cursor));
                if (idAfter != null) {
                    condition = condition.or(
                            feed.likeCount.eq(Integer.parseInt(cursor))
                                    .and(feed.id.lt(idAfter))
                    );
                }
                return condition;
            } else if (sortDirection.equals("ASCENDING")) {
                BooleanExpression condition = feed.likeCount.lt(Integer.parseInt(cursor));
                if (idAfter != null) {
                    condition = condition.or(
                            feed.likeCount.eq(Integer.parseInt(cursor))
                                    .and(feed.id.lt(idAfter))
                    );
                }
                return condition;
            }
        }
        return null;
    }

    private OrderSpecifier<?> getSortOrder(String sortBy, SortDirection sortDirection) {
        if (sortBy.equals("createdAt")) {
        return sortDirection.equals(SortDirection.DESCENDING)
                    ? feed.createdAt.desc()
                    : feed.createdAt.asc();
        } else if (sortBy.equals("likes")) {
            return sortDirection.equals(SortDirection.DESCENDING)
                    ? feed.likeCount.desc()
                    : feed.likeCount.asc();
        }
        return null;
    }
}

