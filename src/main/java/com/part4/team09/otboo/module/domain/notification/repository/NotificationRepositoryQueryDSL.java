package com.part4.team09.otboo.module.domain.notification.repository;

import com.part4.team09.otboo.module.domain.notification.entity.Notification;
import com.part4.team09.otboo.module.domain.notification.entity.QNotification;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Repository
@RequiredArgsConstructor
public class NotificationRepositoryQueryDSL {

    private final JPAQueryFactory queryFactory;
    private final QNotification note = QNotification.notification;

    // 알림 목록 조회
    public List<Notification> getNotifications(UUID loginUserId, LocalDateTime cursor, UUID idAfter, int limit) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(note.receiverId.eq(loginUserId)); // 알림 수신자 기준

        // 커서 페이징 조건
        if (cursor != null) {
            BooleanBuilder cursorCond = new BooleanBuilder();
            cursorCond.or(note.createdAt.lt(cursor));
            if (idAfter != null) {
                cursorCond.or(note.createdAt.eq(cursor).and(note.id.lt(idAfter)));
            }
            condition.and(cursorCond);
        }

        return queryFactory
                .selectFrom(note)
                .where(condition)
                .orderBy(note.createdAt.desc(), note.id.desc())
                .limit(limit)
                .fetch();
    }

    // 알림 개수
    public int countNotifications(UUID loginUserId) {
        return Math.toIntExact(
                queryFactory
                        .select(note.count())
                        .from(note)
                        .where(note.receiverId.eq(loginUserId))
                        .fetchOne()
        );
    }
}
