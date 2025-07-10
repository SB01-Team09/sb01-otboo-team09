package com.part4.team09.otboo.module.domain.directmessage.repository;

import com.part4.team09.otboo.module.domain.directmessage.entity.DirectMessage;
import com.part4.team09.otboo.module.domain.directmessage.entity.QDirectMessage;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DirectMessageRepositoryQueryDSL {

    private final JPAQueryFactory queryFactory;
    private final QDirectMessage dm = QDirectMessage.directMessage;

    // DM 목록 조회
    public List<DirectMessage> getDirectMessages(UUID userId, UUID currentUserId, LocalDateTime cursor, UUID idAfter, int limit) {

        BooleanBuilder condition = new BooleanBuilder();

        // 양방향 DM 조건
        condition.and(
                dm.senderId.eq(currentUserId).and(dm.receiverId.eq(userId))
                        .or(dm.senderId.eq(userId).and(dm.receiverId.eq(currentUserId)))
        );

        // 커서 페이징 조건
        if (cursor != null) {
            BooleanBuilder cursorCond = new BooleanBuilder();
            cursorCond.or(dm.createdAt.gt(cursor));
            if (idAfter != null) {
                cursorCond.or(dm.createdAt.eq(cursor).and(dm.id.gt(idAfter)));
            }
            condition.and(cursorCond);
        }

        return queryFactory
                .selectFrom(dm)
                .where(condition)
                .orderBy(dm.createdAt.asc(), dm.id.asc())
                .limit(limit)
                .fetch();
    }

    // DM 개수
    public int countDirectMessages(UUID userId, UUID currentUserId) {
        return Math.toIntExact(
                queryFactory
                    .select(dm.count())
                    .from(dm)
                    .where(
                            dm.senderId.eq(currentUserId).and(dm.receiverId.eq(userId))
                                    .or(dm.senderId.eq(userId).and(dm.receiverId.eq(currentUserId)))
                    )
                    .fetchOne()
        );
    }
}
