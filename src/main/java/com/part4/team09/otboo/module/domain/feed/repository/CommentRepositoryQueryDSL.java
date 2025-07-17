package com.part4.team09.otboo.module.domain.feed.repository;

import com.part4.team09.otboo.module.domain.feed.entity.Comment;
import com.part4.team09.otboo.module.domain.feed.entity.QComment;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryQueryDSL {

    private final JPAQueryFactory queryFactory;

    QComment comment = QComment.comment;

    // 댓글 목록 조회
    public List<Comment> getComments(UUID feedId, String cursor, UUID idAfter, int limit) {
        return queryFactory
                .selectFrom(comment)
                .where(
                  comment.feedId.eq(feedId),
                  cursorCondition(cursor, idAfter)
                )
                .orderBy(comment.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    // 댓글 개수
    public int countComments(UUID feedId){
        Long count = queryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.feedId.eq(feedId))
                .fetchOne();
        return count != null ? Math.toIntExact(count) : 0;
    }


    private BooleanExpression cursorCondition(String cursor, UUID idAfter) {
      if (cursor == null || cursor.isBlank()) {
        return null;
      }
        LocalDateTime decodedCursor = LocalDateTime.parse(cursor);

        BooleanExpression condition = comment.createdAt.gt(decodedCursor);

        if (idAfter != null) {
            condition = condition.or(
                    comment.createdAt.eq(decodedCursor).and(comment.id.gt(idAfter))
            );
        }
        return condition;
    }
}
