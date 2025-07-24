package com.part4.team09.otboo.module.domain.follow.dto;


import java.time.LocalDateTime;
import java.util.UUID;

public record FollowListRequest(
        UUID userId,
        LocalDateTime cursor,
        UUID idAfter,
        int limit,
        String nameLike
) {
}
