package com.part4.team09.otboo.module.domain.follow.event;

import java.util.UUID;

public record FollowDeletedEvent(
        UUID followeeId,
        UUID followerId
) {
}
