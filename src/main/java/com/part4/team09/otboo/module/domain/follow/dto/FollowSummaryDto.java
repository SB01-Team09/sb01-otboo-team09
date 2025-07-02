package com.part4.team09.otboo.module.domain.follow.dto;

import java.util.UUID;

public record FollowSummaryDto(
        UUID followeeId,
        int followerCount,
        int followingCount,
        boolean followedByMe,
        UUID followedByMeId,
        boolean followingMe

) {
}
