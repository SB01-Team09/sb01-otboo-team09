package com.part4.team09.otboo.module.domain.directmessage.repository;

import com.part4.team09.otboo.module.domain.directmessage.entity.DirectMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

    @Query("""
    SELECT dm
    FROM DirectMessage dm
    WHERE 
        (
            (dm.senderId = :currentUserId AND dm.receiverId = :userId)
            OR
            (dm.senderId = :userId AND dm.receiverId = :currentUserId)
        )
        AND (
            (:cursor IS NULL)
            OR (dm.createdAt > :cursor)
            OR (dm.createdAt = :cursor AND dm.id > :idAfter)
        )
    ORDER BY dm.createdAt ASC, dm.id ASC
    """)
    List<DirectMessage> getDirectMessages(
            @Param("userId") UUID userId,
            @Param("currentUserId") UUID currentUserId,
            @Param("cursor") LocalDateTime cursor,
            @Param("idAfter") UUID idAfter,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(dm)
    FROM DirectMessage dm
    WHERE
        (
            (dm.senderId = :currentUserId AND dm.receiverId = :userId)
            OR
            (dm.senderId = :userId AND dm.receiverId = :currentUserId)
    )
""")
    int countDirectMessages(@Param("userId") UUID userId, @Param("currentUserId") UUID currentUserId);
}
