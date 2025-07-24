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

    // DM 목록 조회
    public List<Notification> getNotifications(LocalDateTime cursor, UUID idAfter, int limit) {
        // 커서 페이징 조건
        BooleanBuilder cursorCond = new BooleanBuilder();
        if (cursor != null) {
            cursorCond.or(note.createdAt.lt(cursor));
            if (idAfter != null) {
                cursorCond.or(note.createdAt.eq(cursor).and(note.id.lt(idAfter)));
            }
        }

        return queryFactory
                .selectFrom(note)
                .where(cursorCond)
                .orderBy(note.createdAt.desc(), note.id.desc())
                .limit(limit)
                .fetch();
    }

    // DM 개수
    public int countNotifications() {
        return Math.toIntExact(
                queryFactory
                        .select(note.count())
                        .from(note)
                        .fetchOne()
        );
    }
}
