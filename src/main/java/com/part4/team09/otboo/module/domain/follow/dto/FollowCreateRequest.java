package com.part4.team09.otboo.module.domain.follow.dto;

import java.util.UUID;

public record FollowCreateRequest(
        UUID followeeId,
        UUID followerId

) {
}
