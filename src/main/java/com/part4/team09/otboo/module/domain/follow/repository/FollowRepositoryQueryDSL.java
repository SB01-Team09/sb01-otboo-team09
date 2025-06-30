package com.part4.team09.otboo.module.domain.follow.repository;

import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.part4.team09.otboo.module.domain.follow.entity.QFollow.follow;
import static com.part4.team09.otboo.module.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryQueryDSL {

    private final JPAQueryFactory queryFactory;

    // 검색 O : QueryDSL

    // 팔로잉 목록 조회: idAfter가 null이면 처음부터 반환, null이 아니면 커서페이징대로 진행
    public List<Follow> getFollowings(UUID followerId, UUID idAfter, LocalDateTime createdAtAfter, String nameLike, Pageable pageable) {
        return queryFactory
                .selectFrom(follow)
                .join(user).on(follow.followeeId.eq(user.id))
                .where(
                        follow.followerId.eq(followerId),
                        user.name.likeIgnoreCase("%" + nameLike + "%"),
                        cursorCondition(createdAtAfter, idAfter) // null일 때 처리를 위해 메서드로 따로 뺐습니다
                )
                .orderBy(follow.createdAt.desc(), follow.id.desc())
                .limit(pageable.getPageSize())
                .fetch();
    }


    // 팔로잉 목록 개수 (totalCount)
    public int countFollowings(UUID followerId, String nameLike){
        Long count = queryFactory
                .select(follow.count())
                .from(follow)
                .join(user).on(follow.followeeId.eq(user.id))
                .where(
                        follow.followerId.eq(followerId),
                        user.name.likeIgnoreCase("%" + nameLike + "%")
                )
                .fetchOne();

        return count != null ? Math.toIntExact(count) : 0;
        // SQL 에선 count를 BIGINT로 반환하는데 QueryDSL은 이를 Long으로 받아옴. 그래서 toIntExact메서드로 int로 변환
        // (int)(long)count 캐스팅은 범위 초과시 에러를 던지지 않는데 toIntExact는 던지기 때문에 더 안전한 방법
    }


    // 검색어 팔로잉 조회 커서조건
    private BooleanExpression cursorCondition(LocalDateTime createdAtAfter, UUID idAfter) {
        if (createdAtAfter == null || idAfter == null) {
            return null;
        }

        return follow.createdAt.lt(createdAtAfter)
                .or(follow.createdAt.eq(createdAtAfter).and(follow.id.lt(idAfter)));
    }

}
