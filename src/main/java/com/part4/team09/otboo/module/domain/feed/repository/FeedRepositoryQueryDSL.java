package com.part4.team09.otboo.module.domain.feed.repository;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedListRequest;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.entity.QFeed;
import com.part4.team09.otboo.module.domain.feed.entity.QOotd;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.QPrecipitation;
import com.part4.team09.otboo.module.domain.weather.entity.QWeather;
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
    QWeather weather = QWeather.weather;
    QPrecipitation precipitation = QPrecipitation.precipitation;


    // 피드 목록 조회
    public List<Feed> getFeeds(FeedListRequest request) {
        return queryFactory
                .selectFrom(feed)
                .join(ootd).on(feed.id.eq(ootd.feedId)).fetchJoin() // feed, ootd 조인
                .join(weather).on(feed.weatherId.eq(weather.id)).fetchJoin()  // feed, weather 조인
                .join(precipitation).on(weather.precipitationId.eq(precipitation.id)).fetchJoin()  // weather, precipitation 조인
                .where(
                        keywordLikeCondition(request.keywordLike()),
                        skyStatusCondition(request.skyStatusEqual()),
                        precipitationCondition(request.precipitationTypeEqual()),
                        equalAuthorId(request.authorIdEqual()),
                        cursorCondition(request.cursor(), request.idAfter(), request.sortBy(), request.sortDirection())
                )
                .orderBy(getSortOrder(request.sortBy(), request.sortDirection()))
                .limit(request.limit()+1)
                .fetch();
    }

    // 피드 개수
    public int countFeeds(FeedListRequest request){
        Long count = queryFactory
                .select(feed.count())
                .from(feed)
                .join(ootd).on(feed.id.eq(ootd.feedId))// feed, ootd 조인
                .join(weather).on(feed.weatherId.eq(weather.id))// feed, weather 조인
                .join(precipitation).on(weather.precipitationId.eq(precipitation.id)) // weather, precipitation 조인
                .where(
                        keywordLikeCondition(request.keywordLike()),
                        skyStatusCondition(request.skyStatusEqual()),
                        precipitationCondition(request.precipitationTypeEqual()),
                        equalAuthorId(request.authorIdEqual())
                )
                .fetchOne();
        return count != null ? Math.toIntExact(count) : 0;
    }


    private BooleanExpression keywordLikeCondition(String keyword) {
        return keyword != null && !keyword.isBlank() ? feed.content.likeIgnoreCase("%" + keyword + "%") : null;
    }
    private BooleanExpression skyStatusCondition(Weather.SkyStatus skyStatusEqual) {
        return skyStatusEqual != null ? weather.skyStatus.eq(skyStatusEqual) : null;
    }
    private BooleanExpression precipitationCondition(Precipitation.PrecipitationType precipitationTypeEqual) {
        return precipitationTypeEqual != null ? precipitation.type.eq(precipitationTypeEqual) : null;
    }

    private BooleanExpression equalAuthorId(UUID authorId) {
        return authorId != null ? feed.authorId.eq(authorId) : null;
    }

    private BooleanExpression cursorCondition(String cursor, UUID idAfter, String sortBy, SortDirection sortDirection) {

        if (cursor == null || cursor.isBlank()) return null;

        if ("createdAt".equals(sortBy)) {
            LocalDateTime cursorTime = LocalDateTime.parse(cursor);
            if (SortDirection.DESCENDING.equals(sortDirection)) {
                BooleanExpression condition = feed.createdAt.lt(cursorTime);
                if (idAfter != null) {
                    condition = feed.createdAt.lt(cursorTime)
                            .or(feed.createdAt.eq(cursorTime).and(feed.id.lt(idAfter)));
                }
                return condition;
            } else if (SortDirection.ASCENDING.equals(sortDirection)) {
                BooleanExpression condition = feed.createdAt.gt(cursorTime);
                if (idAfter != null) {
                    condition = feed.createdAt.gt(cursorTime)
                            .or(feed.createdAt.eq(cursorTime).and(feed.id.gt(idAfter)));
                }
                return condition;
            }
        }
        if ("likeCount".equals(sortBy)) {
            int cursorLikeCount = Integer.parseInt(cursor);
            if (SortDirection.DESCENDING.equals(sortDirection)) {
                if (idAfter != null) {
                    return feed.likeCount.lt(cursorLikeCount)
                            .or(feed.likeCount.eq(cursorLikeCount).and(feed.id.lt(idAfter)));
                } else {
                    return feed.likeCount.lt(cursorLikeCount);
                }
            } else if (SortDirection.ASCENDING.equals(sortDirection)) {
                if (idAfter != null) {
                    return feed.likeCount.gt(cursorLikeCount)
                            .or(feed.likeCount.eq(cursorLikeCount).and(feed.id.gt(idAfter)));
                } else {
                    return feed.likeCount.gt(cursorLikeCount);
                }
            }
        }
        return null;
    }

    private OrderSpecifier<?> getSortOrder(String sortBy, SortDirection sortDirection) {
        if ("createdAt".equals(sortBy)) {
        return SortDirection.DESCENDING.equals(sortDirection)
                    ? feed.createdAt.desc()
                    : feed.createdAt.asc();
        } else if ("likeCount".equals(sortBy)) {
            return SortDirection.DESCENDING.equals(sortDirection)
                    ? feed.likeCount.desc()
                    : feed.likeCount.asc();
        }
        return null;
    }
}

