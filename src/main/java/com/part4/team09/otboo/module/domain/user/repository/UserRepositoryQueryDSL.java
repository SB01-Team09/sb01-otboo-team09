package com.part4.team09.otboo.module.domain.user.repository;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.user.dto.request.UserListRequest;
import com.part4.team09.otboo.module.domain.user.entity.QUser;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.part4.team09.otboo.module.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class UserRepositoryQueryDSL {

    private final JPAQueryFactory queryFactory;

    // 계정 목록 조회
    public List<User> getUsers(UserListRequest request) {
        QUser user = QUser.user;

        return queryFactory
                .selectFrom(user)
                .where(
                        likeEmail(request.emailLike()),
                        equalRole(request.roleEqual()),
                        equalLocked(request.locked()),
                        cursorCondition(request.cursor(), request.idAfter())
                )
                .orderBy(getSortOrder(request.sortBy(), request.sortDirection()))
                .limit(request.limit() + 1)
                .fetch();
    }

    // 계정 목록 개수 (totalCount)
    public int countUsers(UserListRequest request){
        Long count = queryFactory
                .select(user.count())
                .from(user)
                .where(
                        likeEmail(request.emailLike()),
                        equalRole(request.roleEqual()),
                        equalLocked(request.locked())
                )
                .fetchOne();

        return count != null ? Math.toIntExact(count) : 0;
    }

    private BooleanExpression likeEmail(String emailLike) {
        return emailLike != null ? QUser.user.email.likeIgnoreCase("%" + emailLike + "%") : null;
    }

    private BooleanExpression equalRole(User.Role role) {
        return role != null ? QUser.user.role.eq(User.Role.valueOf(role.name())) : null;
    }

    private BooleanExpression equalLocked(Boolean locked) {
        return locked != null ? QUser.user.locked.eq(locked) : null;
    }

    private BooleanExpression cursorCondition(String cursor, UUID idAfter) {
        if (cursor == null) return null;

        BooleanExpression condition = QUser.user.email.gt(cursor);

        if (idAfter != null) {
            condition = condition.or(
                    QUser.user.email.eq(cursor).and(QUser.user.id.gt(idAfter))
            );
        }
        return condition;
    }

    private OrderSpecifier<?> getSortOrder(String sortBy, SortDirection direction) {
        if (sortBy == null || direction == null) {
            throw new IllegalArgumentException("sortBy와 sortDirection은 필수입니다.");
        }

        PathBuilder<User> entityPath = new PathBuilder<>(User.class, "user");
        Order order = direction == SortDirection.ASCENDING ? Order.ASC : Order.DESC;
        return new OrderSpecifier<>(order, entityPath.get(sortBy, Comparable.class));
    }
}

