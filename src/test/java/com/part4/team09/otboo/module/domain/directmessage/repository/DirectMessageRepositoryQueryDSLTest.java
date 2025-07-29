package com.part4.team09.otboo.module.domain.directmessage.repository;

import com.part4.team09.otboo.module.domain.directmessage.entity.DirectMessage;
import com.part4.team09.otboo.module.domain.directmessage.entity.QDirectMessage;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectMessageRepositoryQueryDSLTest {

    @Mock
    private JPAQueryFactory queryFactory;

    @InjectMocks
    private DirectMessageRepositoryQueryDSL directMessageRepositoryQueryDSL;

    private QDirectMessage dm = QDirectMessage.directMessage;

    @Test
    @DisplayName("getDirectMessages - 커서 없이 기본 조회")
    void getDirectMessages_noCursor() {
        UUID userId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        JPAQuery<DirectMessage> jpaQuery = mock(JPAQuery.class);

        // 조건에 따라 호출될 메서드들 mocking
        when(queryFactory.selectFrom(dm)).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class), any(OrderSpecifier.class))).thenReturn(jpaQuery);
        when(jpaQuery.limit(anyLong())).thenReturn(jpaQuery);

        // 결과용 리스트
        List<DirectMessage> expected = List.of(mock(DirectMessage.class));
        when(jpaQuery.fetch()).thenReturn(expected);

        // 실제 메서드 호출
        List<DirectMessage> result = directMessageRepositoryQueryDSL.getDirectMessages(userId, currentUserId, null, null, 10);

        assertThat(result).isEqualTo(expected);

        // 메서드 호출 확인
        verify(queryFactory).selectFrom(dm);
        verify(jpaQuery).where(any(Predicate.class));
        verify(jpaQuery).orderBy(dm.createdAt.desc(), dm.id.desc());
        verify(jpaQuery).limit(10);
        verify(jpaQuery).fetch();
    }

    @Test
    @DisplayName("getDirectMessages - 커서와 idAfter 조건 포함 조회")
    void getDirectMessages_withCursorAndIdAfter() {
        UUID userId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        LocalDateTime cursor = LocalDateTime.now().minusDays(1);
        UUID idAfter = UUID.randomUUID();

        JPAQuery<DirectMessage> jpaQuery = mock(JPAQuery.class);

        when(queryFactory.selectFrom(dm)).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class), any(OrderSpecifier.class))).thenReturn(jpaQuery);
        when(jpaQuery.limit(anyLong())).thenReturn(jpaQuery);

        List<DirectMessage> expected = List.of(mock(DirectMessage.class));
        when(jpaQuery.fetch()).thenReturn(expected);

        List<DirectMessage> result = directMessageRepositoryQueryDSL.getDirectMessages(userId, currentUserId, cursor, idAfter, 5);

        assertThat(result).isEqualTo(expected);

        verify(queryFactory).selectFrom(dm);
        verify(jpaQuery).where(any(Predicate.class));
        verify(jpaQuery).orderBy(dm.createdAt.desc(), dm.id.desc());
        verify(jpaQuery).limit(5);
        verify(jpaQuery).fetch();
    }

    @Test
    @DisplayName("countDirectMessages - 개수 조회")
    void countDirectMessagesSuccess() {
        UUID userId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        // Mock QueryDSL 반환 타입 Long
        JPAQuery<Long> countQuery = mock(JPAQuery.class);

        when(queryFactory.select(dm.count())).thenReturn(countQuery);
        when(countQuery.from(dm)).thenReturn(countQuery);
        when(countQuery.where(any(Predicate.class))).thenReturn(countQuery);
        when(countQuery.fetchOne()).thenReturn(7L);

        int count = directMessageRepositoryQueryDSL.countDirectMessages(userId, currentUserId);

        assertThat(count).isEqualTo(7);

        verify(queryFactory).select(dm.count());
        verify(countQuery).from(dm);
        verify(countQuery).where(any(Predicate.class));
        verify(countQuery).fetchOne();
    }
}
