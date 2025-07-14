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
                .join(ootd).on(feed.id.eq(ootd.feedId)) // feed, ootd 조인
                .join(weather).on(feed.weatherId.eq(weather.id)) // feed, weather 조인
                .join(precipitation).on(weather.precipitationId.eq(precipitation.id)) // weather, precipitation 조인
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
                .join(weather).on(feed.weatherId.eq(weather.id)) // feed, weather 조인
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

        if (sortBy.equals("likeCount")) {
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
        } else if (sortBy.equals("likeCount")) {
            return sortDirection.equals(SortDirection.DESCENDING)
                    ? feed.likeCount.desc()
                    : feed.likeCount.asc();
        }
        return null;
    }
}

